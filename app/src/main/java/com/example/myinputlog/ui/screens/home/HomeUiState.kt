package com.example.myinputlog.ui.screens.home

import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.models.CourseHeaderUiModel

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object NetworkError : HomeUiState
    data class Success(
        val courseHeader: CourseHeaderUiModel = CourseHeaderUiModel(),
        val userCourses: List<CourseUiModel> = listOf(),
        val confettiColors: List<Long> = listOf(),
        val isParty: Boolean = false
    ) : HomeUiState
}