package com.example.myinputlog.ui.screens.video

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.remote.toVideoMetadata
import com.example.myinputlog.data.repository.VideoDataRepository
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.StorageService
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import com.example.myinputlog.ui.screens.utils.Country
import com.example.myinputlog.ui.screens.utils.ext.extractYouTubeVideoId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    private val videoId: String = checkNotNull(savedStateHandle[VideoDestination.VIDEO_ID_ARG])
    private val defaultCourseId: String =
        checkNotNull(savedStateHandle[VideoDestination.COURSE_ID_ARG])
    private val initialVideoUrl: String =
        checkNotNull(savedStateHandle[VideoDestination.VIDEO_URL_ARG])

    private val userIdFlow = accountService.currentUser.map { it.id }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userCoursesFlow: Flow<List<UserCourse>?> = userIdFlow.flatMapLatest { id ->
        if (id.isEmpty()) {
            flowOf(null)
        } else {
            storageService.getUserCourses(id)
        }
    }

    private val _userDraft = MutableStateFlow(VideoUserDraft(selectedCourseId = defaultCourseId))
    private val _videoMetadata = MutableStateFlow(VideoMetadata())
    private val _loadingState = MutableStateFlow<VideoLoadState>(VideoLoadState.LoadingFromStorage)
    private val _uiFlags = MutableStateFlow(VideoUiFlags())

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
                isFormValid = draft.videoUrl.isNotBlank() && loadState is VideoLoadState.Success
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
            val userId = accountService.currentUser.first().id
            if (videoId != DEFAULT_ID.toString()) {
                val video = storageService.getYouTubeVideo(userId, defaultCourseId, videoId)
                if (video != null) {
                    _videoMetadata.value = video.toVideoMetadata()
                    _userDraft.value = video.toVideoUserDraft(defaultCourseId)
                    _loadingState.value = VideoLoadState.Success
                } else {
                    _loadingState.value = VideoLoadState.StorageError
                }
            } else {
                _userDraft.update { it.copy(selectedCourseId = defaultCourseId) }
                _loadingState.value = VideoLoadState.Success
            }
            if (initialVideoUrl.isNotBlank()) {
                loadVideoMetadata { }
            }
        }
    }

    fun loadVideoMetadata(callback: (Int) -> Unit) {
        viewModelScope.launch {
            val videoId = _userDraft.value.videoUrl.extractYouTubeVideoId() ?: ""
            try {
                videoDataRepository.getVideoData(videoId).let {
                    if (it.isSuccessful) {
                        val videoData = it.body()
                        Log.d(TAG, videoData.toString())
                        val videoMetadata = videoData?.toVideoMetadata()
                        if (videoMetadata != null) {
                            Log.d(TAG, videoMetadata.toString())
                            _videoMetadata.value = videoMetadata
                            _loadingState.value = VideoLoadState.Success
//                            _videoScreenState.update { videoUiState ->
//                                videoData.toYouTubeVideo()?.toVideoUiState()?.copy(
//                                    id = videoUiState.id,
//                                    videoUrl = videoUiState.videoUrl,
//                                    selectedCourseId = videoUiState.selectedCourseId,
//                                    userCourses = videoUiState.userCourses,
//
//                                    speakersNationality = videoUiState.speakersNationality,
//                                    isLoading = false,
//                                    isEdit = videoUiState.isEdit,
//                                    networkError = false
//                                ) ?: videoUiState.copy()
//                            }
//                            validateForm()
                            callback(0)
                        } else {
                            // network ok, data error
                            _loadingState.value = VideoLoadState.MetadataError
                            Log.d(TAG, "data sie zesrało")
                            callback(1)
                        }
                    } else {
                        // actual network error
                        _loadingState.value = VideoLoadState.NetworkError
                        Log.d(TAG, "network sie zesrało")
                        callback(2)
                    }
                }
            } catch (e: Exception) {
                // some other error
                e.message?.let { Log.d(TAG, it) }
                Log.d(TAG, "coś innego sie zesrało")
                _loadingState.value = VideoLoadState.NetworkError
                callback(2)
            }
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
        _userDraft.update { it.copy(selectedCourseId = newCourse.id) }
    }

    fun updateVideoUrl(newUrl: String) {
        _userDraft.update { it.copy(videoUrl = newUrl) }
//        loadVideoMetadata { }
    }

    fun updateLanguage(newLanguage: Country? = null) {
        _userDraft.update { it.copy(speakersNationality = newLanguage) }
    }

    fun updateWatchedOn(milliseconds: Long?) {
        _userDraft.update { it.copy(watchedOn = milliseconds?.let { Date(it) }) }
    }

    fun deleteVideo() {
        toggleDeleteDialogVisibility(false)
        viewModelScope.launch {
            val userId = accountService.currentUser.first().id
            storageService.deleteYouTubeVideo(userId, defaultCourseId, videoId)
        }
    }

    fun persistVideo() {
        viewModelScope.launch {
            val currentState = videoUiState.value
            if (currentState is VideoUiState.Success) {
                val video = currentState.toYouTubeVideo()
                val userId = accountService.currentUser.first().id
                val selectedCourseId = currentState.videoUserDraft.selectedCourseId
                if (video.id.isBlank()) {
                    storageService.saveYouTubeVideo(userId, selectedCourseId, video)
                } else {
                    storageService.updateYouTubeVideo(userId, defaultCourseId, video)
                }
            }
        }
    }

    companion object {
        private const val TAG = "VideoViewModel"
    }
}