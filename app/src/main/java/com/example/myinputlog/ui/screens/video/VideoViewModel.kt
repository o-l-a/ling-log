package com.example.myinputlog.ui.screens.video

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.R
import com.example.myinputlog.data.remote.getChannelId
import com.example.myinputlog.data.remote.getChannelTitle
import com.example.myinputlog.data.repository.ApiDataRepository
import com.example.myinputlog.data.repository.DataResult
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.toCourseUiModel
import com.example.myinputlog.ui.models.toLabelUiModel
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import com.example.myinputlog.ui.navigation.VideoRoute
import com.example.myinputlog.ui.screens.common.UiText
import com.example.myinputlog.ui.screens.common.ext.extractYouTubeVideoId
import com.example.myinputlog.ui.screens.common.ext.stripUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val apiDataRepository: ApiDataRepository,
    private val storageDataRepository: StorageDataRepository
) : ViewModel() {
    sealed class VideoUiEvent {
        data class ShowSnackbar(val message: UiText) : VideoUiEvent()
        object NavigateBack : VideoUiEvent()
    }

    private val videoRoute = savedStateHandle.toRoute<VideoRoute>()
    private val defaultCourseId: String = videoRoute.courseId
    private val videoId = sanitizeInitialVideoId(videoRoute.videoId)

    private val _videoForm = MutableStateFlow(VideoForm())
    private val _loadingState = MutableStateFlow<VideoLoadState>(VideoLoadState.LoadingFromStorage)
    private val _uiFlags = MutableStateFlow(VideoUiFlags())

    private val _uiEvent = Channel<VideoUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var fetchJob: Job? = null

    @OptIn(FlowPreview::class)
    val suggestions: StateFlow<List<LabelUiModel>> = combine(
        _videoForm.map { it.searchQuery }.distinctUntilChanged().debounce(100),
        _videoForm.map { it.selectedLabels }.distinctUntilChanged(),
        _videoForm.map { it.allLabels }.distinctUntilChanged()
    ) { query, selected, all ->
        if (query.isEmpty()) emptyList()
        else {
            all.filter { label ->
                label.title.contains(
                    query, ignoreCase = true
                ) && selected.none { it.id == label.id }
            }.sortedWith(compareByDescending<LabelUiModel> {
                it.title.startsWith(
                    query, ignoreCase = true
                )
            }.thenBy { it.title.lowercase() })
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoUiState: StateFlow<VideoUiState> = combine(
        storageDataRepository.courses, _videoForm, _loadingState, _uiFlags, suggestions
    ) { courses, form, loadState, flags, suggestions ->
        if (flags.isDeleting) {
            VideoUiState.Loading
        } else if (loadState is VideoLoadState.StorageError) {
            VideoUiState.Error
        } else {
            VideoUiState.Success(
                videoForm = form,
                videoLoadState = loadState,
                userCourses = courses.map { it.toCourseUiModel() },
                videoUiFlags = flags,
                suggestions = suggestions.toSet(),
                isFormValid = form.videoId.isNotBlank() && loadState is VideoLoadState.Success,
                isDeleteEnabled = !isNewVideo(videoId),
                isSaveEnabled = flags.isEditStarted,
                isCourseEditable = isNewVideo(videoId)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VideoUiState.Loading
    )

    init {
        loadVideoFromStorage()
    }

    private fun loadVideoFromStorage() {
        viewModelScope.launch {
            val allLabels =
                storageDataRepository.getAllLabelsAsSet().map { it.toLabelUiModel() }.toSet()
            val selectedCourse = storageDataRepository.courses.first().firstOrNull { userCourse ->
                userCourse.course.id == defaultCourseId
            }?.toCourseUiModel() ?: CourseUiModel()
            _videoForm.update { it.copy(allLabels = allLabels) }
            if (!isNewVideo(videoId)) {
                loadExistingVideo(selectedCourse)
            } else {
                _videoForm.update { it.copy(selectedCourse = selectedCourse) }
                _loadingState.value = VideoLoadState.Success
            }
        }
    }

    private suspend fun loadExistingVideo(selectedCourse: CourseUiModel) {
        val video = storageDataRepository.getVideo(videoId)
        if (video != null) {
            loadChannel(video.channel.id)
            _videoForm.update {
                it.toFormWithVideoMetadata(video).copy(selectedCourse = selectedCourse)
            }
            _loadingState.value = VideoLoadState.Success
        } else {
            _loadingState.value = VideoLoadState.StorageError
        }
    }

    private suspend fun loadChannel(channelId: String) {
        val channel = storageDataRepository.getChannel(channelId)
        if (channel != null) {
            _videoForm.update { it.toFormWithChannelMetadata(channel) }
            if (isNewVideo(videoId)) {
                _videoForm.update {
                    it.copy(selectedLabels = channel.labels.map { label -> label.toLabelUiModel() }
                        .toSet())
                }
            }
            _uiFlags.update { it.copy(isNewChannel = false) }
            Log.d(TAG, "Loaded channel ${channel.channel.id} from storage")
            return
        } else {
            when (val result = apiDataRepository.getChannelData(channelId)) {
                is DataResult.Success -> {
                    _videoForm.update { it.toFormWithChannelMetadata(result.data) }
                    _uiFlags.update { it.copy(isNewChannel = true) }
                    Log.d(TAG, "Loaded channel ${result.data.getChannelTitle()} from API")
                    return
                }

                else -> {
                    Log.d(TAG, "Failed to load $channelId from API")
                }
            }
        }
        _loadingState.value = VideoLoadState.MetadataError
        _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.wrong_url_message)))
    }

    private suspend fun loadVideoMetadata() {
        val currentUrl = _videoForm.value.videoUrl

        if (currentUrl.isBlank()) {
            _loadingState.value = VideoLoadState.Success
            return
        }

        val videoId = currentUrl.extractYouTubeVideoId() ?: ""
        when (val result = apiDataRepository.getVideoData(videoId)) {
            is DataResult.Success -> {
                val videoItem = result.data
                loadChannel(videoItem.getChannelId())
                _videoForm.update { it.toFormWithVideoMetadata(videoItem) }
                _loadingState.value = VideoLoadState.Success
            }

            is DataResult.ApiError -> {
                _loadingState.value = VideoLoadState.MetadataError
                _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.wrong_url_message)))
                Log.d(TAG, "Network ok, data error")
            }

            is DataResult.NetworkError -> {
                _loadingState.value = VideoLoadState.NetworkError
                _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.network_error)))
                Log.d(TAG, "Network error")
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _videoForm.update { it.copy(searchQuery = newQuery) }
        _uiFlags.update { it.copy(isEditStarted = true) }
    }

    fun startEdit() {
        _uiFlags.update { it.copy(isEditStarted = true) }
    }

    fun addLabel(label: LabelUiModel) {
        val currentLabels = _videoForm.value.selectedLabels
        if (label !in currentLabels) {
            _videoForm.update {
                it.copy(selectedLabels = (currentLabels + label), searchQuery = "")
            }
        }
    }

    fun removeLabel(label: LabelUiModel) {
        _videoForm.update {
            it.copy(
                selectedLabels = it.selectedLabels - label
            )
        }
    }

    fun deleteUrlAndUrlData() {
        _videoForm.update { it.toClearedMetadata().copy(videoUrl = "") }
    }

    fun toggleDeleteDialogVisibility(visible: Boolean) {
        _uiFlags.update { it.copy(isDeleteDialogVisible = visible) }
    }

    fun toggleDatePickerDialogVisibility(visible: Boolean) {
        _uiFlags.update { it.copy(isDatePickerDialogVisible = visible) }
    }

    fun onSyncLabelsChange(checked: Boolean) {
        _videoForm.update { it.copy(saveLabelsForChannel = checked) }
    }

    fun updateUserCourse(newCourse: CourseUiModel) {
        _videoForm.update { it.copy(selectedCourse = newCourse) }
        startEdit()
    }

    fun updateVideoUrl(newUrl: String) {
        val oldUrl = _videoForm.value.videoUrl
        _videoForm.update { it.copy(videoUrl = newUrl) }
        if (oldUrl.stripUrl() != newUrl.stripUrl()) {
            _videoForm.update { it.toClearedMetadata().copy(videoUrl = newUrl) }
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                delay(100) // Wait for user to stop typing
                loadVideoMetadata()
            }
        }
        startEdit()
    }

    fun updateLanguage(newLanguage: String? = null) {
        _videoForm.update { it.copy(speakersNationality = newLanguage) }
        startEdit()
    }

    fun updateWatchedOn(milliseconds: Long?) {
        _videoForm.update { it.copy(watchedOn = milliseconds?.let { date -> Date(date) } as Date) }
        startEdit()
    }

    fun deleteVideo() {
        toggleDeleteDialogVisibility(false)
        viewModelScope.launch {
            _uiFlags.update { it.copy(isDeleting = true) }
            try {
                storageDataRepository.deleteVideo(videoId)
                _uiEvent.send(VideoUiEvent.NavigateBack)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _uiFlags.update { it.copy(isDeleting = false) }
                _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.video_delete_error)))
            }
        }
    }

    fun saveVideo() {
        val currentState = videoUiState.value as? VideoUiState.Success ?: return
        val form = currentState.videoForm

        viewModelScope.launch {
            viewModelScope.launch {
                try {
                    val videoEntity = form.toVideoEntity()
                    val channelEntity = form.toChannelEntity()
                    storageDataRepository.saveVideo(
                        video = videoEntity,
                        channel = channelEntity,
                        labelIds = form.selectedLabels.map { it.id },
                        syncLabelsToChannel = form.saveLabelsForChannel
                    )
                    _uiEvent.send(VideoUiEvent.NavigateBack)
                } catch (e: Exception) {
                    Log.e(TAG, "Save failed", e)
                    _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.video_save_error)))
                }
            }
        }
    }

    private fun sanitizeInitialVideoId(id: String): String {
        return if (id == DEFAULT_ID.toString()) {
            ""
        } else {
            id
        }
    }

    private fun isNewVideo(id: String): Boolean {
        return id.isBlank()
    }

    companion object {
        private const val TAG = "VideoViewModel"
    }
}