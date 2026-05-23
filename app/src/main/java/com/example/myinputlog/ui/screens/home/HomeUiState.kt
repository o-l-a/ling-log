package com.example.myinputlog.ui.screens.home

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.models.CourseHeaderUiModel
import java.time.YearMonth

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object NetworkError : HomeUiState
    data class Success(
        val courseHeader: CourseHeaderUiModel = CourseHeaderUiModel(),
        val userCourses: List<UserCourse> = listOf(),
        val isParty: Boolean = false
    ) : HomeUiState
}