package com.example.myinputlog.ui.screens.recently_watched

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.screens.video_list.VideoListViewModel
import com.example.myinputlog.ui.screens.video_list.customMap
import com.example.myinputlog.ui.screens.video_list.toVideoListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentlyWatchedViewModel @Inject constructor(
    storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService,
) : ViewModel() {
    private val _recentlyWatchedUiState = MutableStateFlow(RecentlyWatchedUiState())
    val recentlyWatchedUiState = _recentlyWatchedUiState.asStateFlow()
    private val userCourses = storageService.userCourses

    init {
        viewModelScope.launch {
            val currentCourse = userCourses.firstOrNull()?.find {
                it.id == (preferenceStorageService.currentCourseId.firstOrNull() ?: "")
            } ?: UserCourse()
            try {
                _recentlyWatchedUiState.update {
                    currentCourse.toRecentlyWatchedUiState().copy(
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.message?.let { Log.d(TAG, it) }
            }
        }
    }

    companion object {
        private const val TAG = "RecentlyWatchedViewModel"
    }
}