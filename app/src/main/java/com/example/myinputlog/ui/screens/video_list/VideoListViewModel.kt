package com.example.myinputlog.ui.screens.video_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.screens.home.HomeUiState
import com.example.myinputlog.ui.screens.home.toHomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService
) : ViewModel() {
    private val _videoListUiState = MutableStateFlow(VideoListUiState())
    val videoListUiState = _videoListUiState.asStateFlow()
    private val userCourses = storageService.userCourses

    init {
        viewModelScope.launch {
            val currentCourse = userCourses.firstOrNull()?.find {
                it.id == (preferenceStorageService.currentCourseId.firstOrNull() ?: "")
            } ?: UserCourse()
            _videoListUiState.update {
                currentCourse.toVideoListUiState().copy(
                    userCourses = userCourses,
                    isLoading = false
                )
            }
        }
    }

    fun updateUiState(updatedUiState: VideoListUiState) {
        _videoListUiState.update {
            updatedUiState.copy()
        }
    }

    fun changeCurrentCourseId(newCourse: UserCourse) {
        viewModelScope.launch {
            preferenceStorageService.saveCurrentCourseId(newCourse.id)
            val currentCourse = userCourses.firstOrNull()?.find {
                it.id == (preferenceStorageService.currentCourseId.firstOrNull() ?: "")
            } ?: UserCourse()
            _videoListUiState.update {
                currentCourse.toVideoListUiState().copy(
                    userCourses = userCourses
                )
            }
        }
    }
}