package com.example.myinputlog.ui.screens.video_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService,
    private val pager: Pager<String, YouTubeVideo>
) : ViewModel() {
    private val _videoListUiState = MutableStateFlow(VideoListUiState())
    val videoListUiState = _videoListUiState.asStateFlow()
    private val userCourses = storageService.userCourses

    init {
        viewModelScope.launch {
            val currentCourse = userCourses.firstOrNull()?.find {
                it.id == (preferenceStorageService.currentCourseId.firstOrNull() ?: "")
            } ?: UserCourse()
            try {
                _videoListUiState.update {
                    currentCourse.toVideoListUiState().copy(
                        userCourses = userCourses,
                        videos = pager.flow.insertHeaderAndSeparators(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.message?.let { Log.d(TAG, it) }
            }
            try {
                val courseStatistics = storageService.getCourseStatistics(currentCourse.id)
                _videoListUiState.update {
                    it.copy(
                        courseStatistics = courseStatistics
                    )
                }
            } catch (e: Exception) {
                e.message?.let { Log.d(TAG, it) }
            }
        }
    }

    fun changeCurrentCourseId(newCourse: UserCourse) {
        viewModelScope.launch {
            preferenceStorageService.saveCurrentCourseId(newCourse.id)
            val currentCourse = userCourses.firstOrNull()?.find {
                it.id == (preferenceStorageService.currentCourseId.firstOrNull() ?: "")
            } ?: UserCourse()
            try {
                _videoListUiState.update {
                    currentCourse.toVideoListUiState().copy(
                        userCourses = userCourses,
                        videos = pager.flow.insertHeaderAndSeparators(),
                    )
                }
            } catch (e: Exception) {
                e.message?.let { Log.d(TAG, it) }
            }
        }
    }

    companion object {
        private const val TAG = "VideoListViewModel"
    }
}

fun Flow<PagingData<YouTubeVideo>>.insertHeaderAndSeparators(): Flow<PagingData<YouTubeVideo>> {
    return this.map {
        it.insertSeparators { before: YouTubeVideo?, after: YouTubeVideo? ->
            when {
                before == null && after != null -> {
                    YouTubeVideo(watchedOn = after.watchedOn)
                }

                before == null || after == null -> {
                    null
                }

                before.watchedOn != after.watchedOn -> {
                    YouTubeVideo(watchedOn = after.watchedOn)
                }

                else -> {
                    null
                }
            }
        }
    }
}