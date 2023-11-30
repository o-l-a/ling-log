package com.example.myinputlog.ui.screens.profile

import com.example.myinputlog.data.model.UserCourse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class ProfileUiState(
    val currentCourseId: String = "",
    val username: String = "",
    val email: String = "",
    val id: String = "",
    val courses: Flow<List<UserCourse>> = emptyFlow(),
    val isDialogVisible: Boolean = false,
    val isNetworkError: Boolean = false,
    val hideEmail: Boolean = false
)