package com.example.myinputlog.ui.screens.course

import com.example.myinputlog.data.model.UserCourse

sealed interface CourseUiState {
    data object Loading : CourseUiState
    data object Error : CourseUiState
    data class Success(
        val courseId: String = "",
        val courseFields: CourseFields,
        val isFormValid: Boolean = false,
        val isDialogVisible: Boolean = false
    ) : CourseUiState
}

data class CourseFields(
    val name: String = "",
    val goalInHours: String = "",
    val otherSourceHours: String = ""
)

fun CourseFields.toUserCourse(id: String): UserCourse = UserCourse(
    id = id,
    name = name,
    goalInHours = goalInHours.toLongOrNull() ?: 0L,
    otherSourceHours = otherSourceHours.toLongOrNull() ?: 0L
)