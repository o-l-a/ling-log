package com.example.myinputlog.ui.screens.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storageService: DefaultStorageService
) : ViewModel() {
    private val _videoUiState = MutableStateFlow(VideoUiState())
    val videoUiState = _videoUiState.asStateFlow()

    private val videoId: String = checkNotNull(savedStateHandle[VideoDestination.videoIdArg])
    private val defaultCourseId: String = checkNotNull(savedStateHandle[VideoDestination.courseIdArg])

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
                isFormValid = it.link.isNotBlank()
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