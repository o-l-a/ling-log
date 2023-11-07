package com.example.myinputlog.ui.screens.home

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.screens.course.CourseUiState
import com.google.firebase.firestore.DocumentId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class HomeUiState(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val goalInHours: String = "",
    val otherSourceHours: String = "",
    val hoursWatched: String = "",

    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val isLoading: Boolean = true
)

fun UserCourse.toHomeUiState(
    isLoading: Boolean = true,
): HomeUiState = HomeUiState(
    id = id,
    name = name,
    goalInHours = goalInHours.toString(),
    otherSourceHours = otherSourceHours.toString(),
    hoursWatched = hoursWatched.toString(),
    isLoading = isLoading,
)

fun HomeUiState.toUserCourse() : UserCourse = UserCourse(
    id = id,
    name = name,
    goalInHours = goalInHours.toFloatOrNull() ?: 0.0F,
    otherSourceHours = otherSourceHours.toFloatOrNull() ?: 0.0F,
    hoursWatched = hoursWatched.toFloatOrNull() ?: 0.0F
)