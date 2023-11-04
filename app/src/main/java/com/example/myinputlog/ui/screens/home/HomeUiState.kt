package com.example.myinputlog.ui.screens.home

import com.example.myinputlog.data.model.UserCourse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

data class HomeUiState(
    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val isLoading: Boolean = true
)