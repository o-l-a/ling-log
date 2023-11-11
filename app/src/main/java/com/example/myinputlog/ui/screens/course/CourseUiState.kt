package com.example.myinputlog.ui.screens.course

import com.example.myinputlog.data.model.UserCourse
import com.google.firebase.firestore.DocumentId

data class CourseUiState(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val goalInHours: String = "",
    val otherSourceHours: String = "",
    val hoursWatched: String = "",

    val isLoading: Boolean = true,
    val isEdit: Boolean = false,
    val isFormValid: Boolean = false,
    val isDialogVisible: Boolean = false
)

fun UserCourse.toCourseUiState(
    isLoading: Boolean = true,
    isEdit: Boolean = false,
    isFormValid: Boolean = false,
    isDialogVisible: Boolean = false,
): CourseUiState = CourseUiState(
    id = id,
    name = name,
    goalInHours = goalInHours.toString(),
    otherSourceHours = otherSourceHours.toString(),
//    hoursWatched = hoursWatched.toString(),
    isLoading = isLoading,
    isEdit = isEdit,
    isFormValid = isFormValid,
    isDialogVisible = isDialogVisible
)

fun CourseUiState.toUserCourse() : UserCourse = UserCourse(
    id = id,
    name = name,
    goalInHours = goalInHours.toLongOrNull() ?: 0L,
    otherSourceHours = otherSourceHours.toLongOrNull() ?: 0L,
)