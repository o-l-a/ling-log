package com.example.myinputlog.ui.screens.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import com.example.myinputlog.ui.screens.course.CourseUiState
import com.example.myinputlog.ui.screens.course.CourseDestination
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
    private val _videoUiState = MutableStateFlow(CourseUiState())
    val videoUiState = _videoUiState.asStateFlow()

    private val videoId: String = checkNotNull(savedStateHandle[VideoDestination.videoIdArg])

    init {
        if (videoId != DEFAULT_ID.toString()) {
            viewModelScope.launch {
                val video = storageService.getYouTubeVideo(videoId)
                if (video != null) {
                    // TODO
//                    updateUiState(product.toProductUiState())
                } else {
                    // TODO("spadł z rowerka")
                }
            }
        } else {
            _videoUiState.update {
                it.copy(
                    isLoading = false,
                    isEdit = true
                )
            }
        }
    }
}