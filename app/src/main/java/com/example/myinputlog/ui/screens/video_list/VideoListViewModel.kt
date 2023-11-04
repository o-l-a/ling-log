package com.example.myinputlog.ui.screens.video_list

import androidx.lifecycle.ViewModel
import com.example.myinputlog.data.service.impl.DefaultStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val storageService: DefaultStorageService
) : ViewModel() {
    private val _videoListUiState = MutableStateFlow(VideoListUiState())
    val videoListUiState = _videoListUiState.asStateFlow()
    val userCourses = storageService.userCourses

    init {
        _videoListUiState.update {
            it.copy(
                userCourses = userCourses,
                isLoading = false
            )
        }
    }
}