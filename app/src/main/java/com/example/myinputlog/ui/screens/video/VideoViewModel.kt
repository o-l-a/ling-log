package com.example.myinputlog.ui.screens.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.model.toYouTubeVideo
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

    init {
        if (videoId != DEFAULT_ID.toString()) {
            viewModelScope.launch {
                val video = storageService.getYouTubeVideo(defaultCourseId, videoId)
                if (video != null) {
                    _videoUiState.update {
                        video.toVideoUiState(isLoading = false).copy()
                    }
                } else {
                    // TODO("this should never happen")
                }
            }
        } else {
            _videoUiState.update {
                it.copy(
                    selectedCourseId = defaultCourseId,
                    isLoading = false,
                    isEdit = true
                )
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

    fun loadVideoData() {
        val videoId = extractYouTubeVideoId(_videoUiState.value.videoUrl) ?: ""
        viewModelScope.launch {
            videoDataRepository.getVideoData(videoId).let {
                if (it.isSuccessful) {
                    val videoData = it.body()
                    if (videoData != null) {
                        _videoUiState.update { videoUiState ->
                            videoData.toYouTubeVideo()?.toVideoUiState()?.copy(
                                videoUrl = videoUiState.videoUrl,
                                selectedCourseId = videoUiState.selectedCourseId,
                                watchedOn = videoUiState.watchedOn,
                                speakersNationality = videoUiState.speakersNationality,
                                isLoading = false,
                                isEdit = true
                            ) ?: videoUiState.copy()
                        }
                        validateForm()
                    }
                } else {

                }
            }
        }
    }

    fun updateUiState(updatedUiState: VideoUiState) {
        _videoUiState.update {
            updatedUiState.copy()
        }
        validateForm()
    }

    fun toggleDialogVisibility(visible: Boolean) {
        _videoUiState.update {
            it.copy(
                isDeleteDialogVisible = visible
            )
        }
    }

    private fun validateForm() {
        _videoUiState.update {
            it.copy(
                isFormValid = it.videoUrl.isNotBlank()
            )
        }
    }

    fun deleteVideo() {
        toggleDialogVisibility(false)
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