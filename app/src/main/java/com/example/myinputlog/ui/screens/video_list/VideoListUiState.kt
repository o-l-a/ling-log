package com.example.myinputlog.ui.screens.video_list

import com.example.myinputlog.data.model.UserCourse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class VideoListUiState(
    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val isLoading: Boolean = true
)