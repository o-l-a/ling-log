package com.example.myinputlog.ui.screens.video

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.R
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.remote.getChannelId
import com.example.myinputlog.data.remote.toChannelMetadata
import com.example.myinputlog.data.remote.toVideoMetadata
import com.example.myinputlog.data.repository.VideoDataRepository
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.StorageService
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import com.example.myinputlog.ui.navigation.VideoRoute
import com.example.myinputlog.ui.screens.utils.Country
import com.example.myinputlog.ui.screens.utils.UiText
import com.example.myinputlog.ui.screens.utils.ext.extractYouTubeVideoId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val storageService: StorageService,
    private val videoDataRepository: VideoDataRepository,
    private val accountService: AccountService
) : ViewModel() {
    sealed class VideoUiEvent {
        data class ShowSnackbar(val message: UiText) : VideoUiEvent()
        object NavigateBack : VideoUiEvent()
    }

    private val videoRoute = savedStateHandle.toRoute<VideoRoute>()
    private val defaultCourseId: String = videoRoute.courseId
    private val videoId = sanitizeInitialVideoId(videoRoute.videoId)
    private val userIdFlow = accountService.currentUser.map { it.id }
    private var originalVideo: YouTubeVideo? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val userCoursesFlow: Flow<List<UserCourse>?> = userIdFlow.flatMapLatest { id ->
        if (id.isEmpty()) {
            flowOf(null)
        } else {
            storageService.getUserCourses(id)
        }
    }

    private val _userDraft = MutableStateFlow(VideoUserDraft())
    private val _videoMetadata = MutableStateFlow(VideoMetadata())
    private val _loadingState = MutableStateFlow<VideoLoadState>(VideoLoadState.LoadingFromStorage)
    private val _uiFlags = MutableStateFlow(VideoUiFlags())

    private val _uiEvent = Channel<VideoUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var fetchJob: Job? = null

    val videoUiState: StateFlow<VideoUiState> = combine(
        userCoursesFlow, _userDraft, _videoMetadata, _loadingState, _uiFlags
    ) { courses, draft, meta, loadState, flags ->
        if (loadState is VideoLoadState.StorageError || courses == null) {
            VideoUiState.Error
        } else {
            VideoUiState.Success(
                id = videoId,
                videoUserDraft = draft,
                videoMetadata = meta,
                videoLoadState = loadState,
                userCourses = courses,
                videoUiFlags = flags,
                isFormValid = draft.videoUrl.isNotBlank() && loadState is VideoLoadState.Success,
                isDeleteEnabled = !isNewVideo(videoId),
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
            val selectedCourse = userCoursesFlow.first()?.firstOrNull { userCourse ->
                userCourse.id == defaultCourseId
            } ?: UserCourse()
            if (!isNewVideo(videoId)) {
                loadExistingVideo(selectedCourse)
            } else {
                _userDraft.update { it.copy(selectedCourse = selectedCourse) }
                _loadingState.value = VideoLoadState.Success
            }
        }
    }

    private suspend fun loadExistingVideo(selectedCourse: UserCourse) {
        val userId = accountService.currentUser.first().id
        val video = storageService.getYouTubeVideo(userId, defaultCourseId, videoId)
        originalVideo = video
        if (video != null) {
            val channelMetadata = loadChannel(userId, video.channelId)
            if (channelMetadata == null) {
                _loadingState.value = VideoLoadState.MetadataError
                _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.wrong_url_message)))
                return
            }
            _videoMetadata.value = video.toVideoMetadata(channelMetadata)
            _userDraft.value = video.toVideoUserDraft(selectedCourse)
            _loadingState.value = VideoLoadState.Success
        } else {
            _loadingState.value = VideoLoadState.StorageError
        }
    }

    private suspend fun loadChannel(userId: String, channelId: String): ChannelMetadata? {
        val channel = storageService.getYouTubeChannel(userId, defaultCourseId, channelId)
        if (channel != null) {
            Log.d(TAG, "Loaded channel ${channel.title} from storage")
            return channel.toChannelMetadata()
        } else {
            videoDataRepository.getChannelData(channelId).let {
                if (it.isSuccessful) {
                    val channelData = it.body()
                    val channelMetadata = channelData?.toChannelMetadata()
                    Log.d(TAG, "Loaded channel ${channelMetadata?.title} from API")
                    return channelMetadata
                } else {
                    return null
                }
            }
        }
    }

    private suspend fun loadVideoMetadata() {
        val currentUrl = _userDraft.value.videoUrl

        if (currentUrl.isBlank()) {
            _loadingState.value = VideoLoadState.Success
            return
        }

        val videoId = currentUrl.extractYouTubeVideoId() ?: ""
        try {
            videoDataRepository.getVideoData(videoId).let {
                if (it.isSuccessful) {
                    val videoData = it.body()
                    val userId = accountService.currentUser.first().id
                    val channelMetadata = loadChannel(userId, videoData?.getChannelId() ?: "")
                    if (channelMetadata == null) {
                        _loadingState.value = VideoLoadState.MetadataError
                        _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.wrong_url_message)))
                        return
                    }
                    val videoMetadata = videoData?.toVideoMetadata(channelMetadata)
                    if (videoMetadata != null) {
                        Log.d(TAG, videoMetadata.toString())
                        _videoMetadata.value = videoMetadata
                        _loadingState.value = VideoLoadState.Success
                    } else {
                        // network ok, data error
                        _loadingState.value = VideoLoadState.MetadataError
                        _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.wrong_url_message)))
                        Log.d(TAG, "Network ok, data error")
                    }
                } else {
                    // actual network error
                    _loadingState.value = VideoLoadState.NetworkError
                    _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.network_error)))
                    Log.d(TAG, "Network error")
                }
            }
        } catch (e: Exception) {
            // some other error
            e.message?.let { Log.d(TAG, it) }
            Log.d(TAG, "Unexpected error")
            _loadingState.value = VideoLoadState.NetworkError
            _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
        }
    }

    fun deleteUrlAndUrlData() {
        _userDraft.update { it.copy(videoUrl = "") }
        _videoMetadata.value = VideoMetadata()
    }

    fun toggleDeleteDialogVisibility(visible: Boolean) {
        _uiFlags.update { it.copy(isDeleteDialogVisible = visible) }
    }

    fun toggleDatePickerDialogVisibility(visible: Boolean) {
        _uiFlags.update { it.copy(isDatePickerDialogVisible = visible) }
    }

    fun updateUserCourse(newCourse: UserCourse) {
        _userDraft.update { it.copy(selectedCourse = newCourse) }
    }

    fun updateVideoUrl(newUrl: String) {
        _userDraft.update { it.copy(videoUrl = newUrl) }
        _videoMetadata.value = VideoMetadata()
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            delay(600) // Wait for user to stop typing
            loadVideoMetadata()
        }
    }

    fun updateLanguage(newLanguage: Country? = null) {
        _userDraft.update { it.copy(speakersNationality = newLanguage) }
    }

    fun updateWatchedOn(milliseconds: Long?) {
        _userDraft.update { it.copy(watchedOn = milliseconds?.let { date -> Date(date) }) }
    }

    fun deleteVideo() {
        toggleDeleteDialogVisibility(false)
        viewModelScope.launch {
            val currentState = videoUiState.value
            if (currentState is VideoUiState.Success) {
                val video = currentState.toYouTubeVideo()
                val userId = accountService.currentUser.first().id
                try {
                    storageService.deleteYouTubeVideo(userId, defaultCourseId, video)
                    _uiEvent.send(VideoUiEvent.NavigateBack)
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    _uiEvent.send(VideoUiEvent.ShowSnackbar(UiText.StringResource(R.string.video_delete_error)))
                }
            }

        }
    }

    fun persistVideo() {
        viewModelScope.launch {
            val currentState = videoUiState.value
            if (currentState is VideoUiState.Success) {
                val video = currentState.toYouTubeVideo()
                val channel = currentState.toYouTubeChannel()
                val userId = accountService.currentUser.first().id
                val selectedCourseId = currentState.videoUserDraft.selectedCourse.id
                try {
                    storageService.saveYouTubeVideo(
                        userId, selectedCourseId, video, originalVideo, channel
                    )
                    _uiEvent.send(VideoUiEvent.NavigateBack)
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
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