package com.example.myinputlog.ui.screens.course

import com.example.myinputlog.data.model.UserCourse

sealed interface CourseUiState {
    data object Loading : CourseUiState
    data object Error : CourseUiState
    data class Success(
        val courseFields: CourseFields,

        val isLoading: Boolean = true,
        val isEdit: Boolean = false,
        val isFormValid: Boolean = false,
        val isDialogVisible: Boolean = false
    ) : CourseUiState
}

data class CourseFields(
    val id: String = "",
    val name: String = "",
    val goalInHours: String = "",
    val otherSourceHours: String = ""
)

fun CourseFields.toUserCourse() : UserCourse = UserCourse(
    id = id,
    name = name,
    goalInHours = goalInHours.toLongOrNull() ?: 0L,
    otherSourceHours = otherSourceHours.toLongOrNull() ?: 0L
)