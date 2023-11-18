package com.example.myinputlog.ui.screens.recently_watched

import androidx.paging.PagingData
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.screens.video_list.VideoListUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class RecentlyWatchedUiState(
    val currentCourseId: String = "",
    val currentCourseName: String = "",
    val videos: Flow<PagingData<YouTubeVideo>> = emptyFlow(),
    val isLoading: Boolean = true
)

fun UserCourse.toRecentlyWatchedUiState(
    isLoading: Boolean = true,
): RecentlyWatchedUiState = RecentlyWatchedUiState(
    currentCourseId = id,
    currentCourseName = name,
    isLoading = isLoading,
)