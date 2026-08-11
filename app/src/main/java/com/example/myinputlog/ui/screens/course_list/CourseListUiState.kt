package com.example.myinputlog.ui.screens.course_list

import com.example.myinputlog.ui.models.CourseUiModel

sealed interface CourseListUiState {
    data object Loading : CourseListUiState
    data object Empty : CourseListUiState
    data object Error : CourseListUiState
    data class Success(
        val userCourses: List<CourseUiModel> = listOf(), val selectedCourse: CourseUiModel? = null
    ) : CourseListUiState
}