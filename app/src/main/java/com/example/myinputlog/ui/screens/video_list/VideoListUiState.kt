package com.example.myinputlog.ui.screens.video_list

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.screens.home.HomeUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class VideoListUiState(
    val currentCourseName: String = "",

    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val isLoading: Boolean = true
)

fun UserCourse.toVideoListUiState(
    isLoading: Boolean = true,
): VideoListUiState = VideoListUiState(
    currentCourseName = name,
    isLoading = isLoading,
)

fun VideoListUiState.toUserCourse() : UserCourse = UserCourse(
    name = currentCourseName
)