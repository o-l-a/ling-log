package com.example.myinputlog.ui.screens.video_list

import androidx.paging.PagingData
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

data class VideoListUiState(
    val currentCourseId: String = "",
    val currentCourseName: String = "",

    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val videos: Flow<PagingData<YouTubeVideo>> = emptyFlow(),
    val isLoading: Boolean = true
)

fun UserCourse.toVideoListUiState(
    isLoading: Boolean = true,
): VideoListUiState = VideoListUiState(
    currentCourseId = id,
    currentCourseName = name,
    isLoading = isLoading,
)

fun VideoListUiState.toUserCourse(): UserCourse = UserCourse(
    id = currentCourseId,
    name = currentCourseName
)