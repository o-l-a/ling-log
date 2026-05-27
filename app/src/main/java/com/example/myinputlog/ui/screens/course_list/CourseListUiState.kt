package com.example.myinputlog.ui.screens.course_list

import com.example.myinputlog.data.model.UserCourse

sealed interface CourseListUiState {
    data object Loading : CourseListUiState
    data object Empty : CourseListUiState
    data object Error : CourseListUiState
    data class Success(val userCourses: List<UserCourse> = listOf()) : CourseListUiState
}