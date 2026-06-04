package com.example.myinputlog.ui.screens.label

import com.example.myinputlog.ui.models.CourseUiModel

sealed interface LabelUiState {
    data object Loading : LabelUiState
    data object Error : LabelUiState
    data class Success(
        val selectedCourse: CourseUiModel = CourseUiModel(),
        val userCourses: List<CourseUiModel> = listOf(),
        val isFormValid: Boolean = false,
        val isDialogVisible: Boolean = false,
        val isDeleteEnabled: Boolean = false,
        val isCourseEditable: Boolean = false
    ) : LabelUiState
}