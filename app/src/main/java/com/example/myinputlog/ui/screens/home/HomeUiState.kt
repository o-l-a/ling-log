package com.example.myinputlog.ui.screens.home

import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.google.firebase.firestore.DocumentId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class HomeUiState(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val goalInHours: String = "",
    val otherSourceHours: String = "",
    val courseStatistics: CourseStatistics = CourseStatistics(),

    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val isLoading: Boolean = true,
    val networkError: Boolean = false,
    val isParty: Boolean = false
)

fun UserCourse.toHomeUiState(
    isLoading: Boolean = true,
    courseStatistics: CourseStatistics = CourseStatistics(),
): HomeUiState = HomeUiState(
    id = id,
    name = name,
    goalInHours = goalInHours.toString(),
    otherSourceHours = otherSourceHours.toString(),
    courseStatistics = courseStatistics,
    isLoading = isLoading,
)

fun HomeUiState.toUserCourse() : UserCourse = UserCourse(
    id = id,
    name = name,
    goalInHours = goalInHours.toLongOrNull() ?: 0L,
    otherSourceHours = otherSourceHours.toLongOrNull() ?: 0L
)