package com.example.myinputlog.ui.screens.label

import com.example.myinputlog.data.model.UserCourse

sealed interface LabelUiState {
    data object Loading : LabelUiState
    data object Error : LabelUiState
    data class Success(
        val selectedCourse: UserCourse = UserCourse(),
        val userCourses: List<UserCourse> = listOf(),
        val isFormValid: Boolean = false,
        val isDialogVisible: Boolean = false,
        val isDeleteEnabled: Boolean = false,
        val isCourseEditable: Boolean = false
    ) : LabelUiState
}