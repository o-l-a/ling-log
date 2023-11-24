package com.example.myinputlog.ui.screens.video

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.remote.toYouTubeVideo
import com.example.myinputlog.data.repository.impl.DefaultVideoDataRepository
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storageService: DefaultStorageService,
    private val videoDataRepository: DefaultVideoDataRepository
) : ViewModel() {
    private val _videoUiState = MutableStateFlow(VideoUiState())
    val videoUiState = _videoUiState.asStateFlow()

    private val videoId: String = checkNotNull(savedStateHandle[VideoDestination.videoIdArg])
    private val defaultCourseId: String =
        checkNotNull(savedStateHandle[VideoDestination.courseIdArg])
    private val videoUrl: String = checkNotNull(savedStateHandle[VideoDestination.videoUrlArg])

    companion object {
        private const val TAG = "VideoViewModel"
    }

    init {
        if (videoId != DEFAULT_ID.toString()) {
            viewModelScope.launch {
                val video = storageService.getYouTubeVideo(defaultCourseId, videoId)
                if (video != null) {
                    _videoUiState.update {
                        video.toVideoUiState(isLoading = false).copy(
                            selectedCourseId = defaultCourseId,
                            userCourses = storageService.userCourses
                        )
                    }
                } else {
                    // TODO("this should never happen")
                }
            }
        } else {
            _videoUiState.update {
                it.copy(
                    selectedCourseId = defaultCourseId,
                    userCourses = storageService.userCourses,
                    isLoading = false,
                    isEdit = true,
                    videoUrl = videoUrl
                )
            }
            if (videoUrl.isNotBlank()) {
                loadVideoData {  }
            }
        }
    }

    private fun extractYouTubeVideoId(url: String): String? {
        val pattern =
            Pattern.compile("http(?:s)?://(?:m\\.)?(?:www\\.)?youtu(?:\\.be/|(?:be-nocookie|be)\\.com/(?:watch|[\\w]+\\?(?:feature=[\\w]+\\.[\\w]+\\&)?v=|v/|e/|embed/|live/|shorts/|user/(?:[\\w#]+/)+))([^&#?\\n]+)")
        val matcher = pattern.matcher(url)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    fun loadVideoData(callback: (Int) -> Unit) {
        val videoId = extractYouTubeVideoId(_videoUiState.value.videoUrl) ?: ""
        viewModelScope.launch {
            try {
                videoDataRepository.getVideoData(videoId).let {
                    if (it.isSuccessful) {
                        val videoData = it.body()
                        if (videoData != null && videoData.items.isNotEmpty()) {
                            Log.d(TAG, videoData.toString())
                            _videoUiState.update { videoUiState ->
                                videoData.toYouTubeVideo()?.toVideoUiState()?.copy(
                                    id = videoUiState.id,
                                    videoUrl = videoUiState.videoUrl,
                                    selectedCourseId = videoUiState.selectedCourseId,
                                    userCourses = videoUiState.userCourses,
                                    watchedOn = videoUiState.watchedOn,
                                    speakersNationality = videoUiState.speakersNationality,
                                    isLoading = false,
                                    isEdit = videoUiState.isEdit,
                                    networkError = false
                                ) ?: videoUiState.copy()
                            }
                            validateForm()
                            callback(0)
                        } else {
                            // network ok, data error
                            updateUiState(videoUiState.value.copy(networkError = true))
                            deleteUrlData()
                            callback(1)
                        }
                    } else {
                        // actual network error
                        updateUiState(videoUiState.value.copy(networkError = true))
                        deleteUrlData()
                        callback(2)
                    }
                }
            } catch (e: Exception) {
                // some other error
                updateUiState(videoUiState.value.copy(networkError = true))
                deleteUrlData()
                callback(2)
            }
        }
    }

    fun updateUiState(updatedUiState: VideoUiState) {
        _videoUiState.update {
            updatedUiState.copy()
        }
        validateForm()
    }

    private fun deleteUrlData() {
        updateUiState(
            videoUiState.value.copy(
                title = "",
                channel = "",
                durationInSeconds = "",
                thumbnailDefaultUrl = "",
                thumbnailMediumUrl = "",
                thumbnailHighUrl = "",
                defaultAudioLanguage = "",
            )
        )
    }

    fun deleteUrlAndUrlData() {
        updateUiState(
            videoUiState.value.copy(
                title = "",
                channel = "",
                durationInSeconds = "",
                videoUrl = "",
                thumbnailDefaultUrl = "",
                thumbnailMediumUrl = "",
                thumbnailHighUrl = "",
                defaultAudioLanguage = "",
            )
        )
    }

    fun toggleDeleteDialogVisibility(visible: Boolean) {
        _videoUiState.update {
            it.copy(
                isDeleteDialogVisible = visible
            )
        }
    }

    fun toggleDatePickerDialogVisibility(visible: Boolean) {
        _videoUiState.update {
            it.copy(
                isDatePickerDialogVisible = visible
            )
        }
    }

    private fun validateForm() {
        _videoUiState.update {
            it.copy(
                isFormValid = it.videoUrl.isNotBlank() && !it.networkError
            )
        }
    }

    fun deleteVideo() {
        toggleDeleteDialogVisibility(false)
        viewModelScope.launch {
            storageService.deleteYouTubeVideo(defaultCourseId, videoId)
        }
    }

    fun persistVideo() {
        viewModelScope.launch {
            val video = _videoUiState.value.toYouTubeVideo()
            val selectedCourseId = _videoUiState.value.selectedCourseId
            if (video.id.isBlank()) {
                storageService.saveYouTubeVideo(selectedCourseId, video)
            } else {
                storageService.updateYouTubeVideo(defaultCourseId, video)
            }
        }
    }
}