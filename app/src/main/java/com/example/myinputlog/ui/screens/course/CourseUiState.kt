package com.example.myinputlog.ui.screens.course

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

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
    hoursWatched = hoursWatched.toString(),
    isLoading = isLoading,
    isEdit = isEdit,
    isFormValid = isFormValid,
    isDialogVisible = isDialogVisible
)

fun CourseUiState.toUserCourse() : UserCourse = UserCourse(
    id = id,
    name = name,
    goalInHours = goalInHours.toFloatOrNull() ?: 0.0F,
    otherSourceHours = otherSourceHours.toFloatOrNull() ?: 0.0F,
    hoursWatched = hoursWatched.toFloatOrNull() ?: 0.0F
)