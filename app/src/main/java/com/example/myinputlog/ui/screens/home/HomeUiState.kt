package com.example.myinputlog.ui.screens.home

import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.YearMonth

data class HomeUiState(
    val id: String = "",
    val name: String = "",
    val goalInHours: String = "",
    val otherSourceHours: String = "",
    val courseStatistics: CourseStatistics = CourseStatistics(),
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val monthlyAggregateData: List<Long> = listOf(),

    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val isLoading: Boolean = true,
    val isCalendarLoading: Boolean = true,
    val networkError: Boolean = false,
    val isParty: Boolean = false
)

fun UserCourse.toHomeUiState(
    isLoading: Boolean = true,
    courseStatistics: CourseStatistics = CourseStatistics(),
    monthlyAggregateData: List<Long> = listOf(),
    selectedYearMonth: YearMonth = YearMonth.now(),
    isCalendarLoading: Boolean = true,
): HomeUiState = HomeUiState(
    id = id,
    name = name,
    goalInHours = goalInHours.toString(),
    otherSourceHours = otherSourceHours.toString(),
    courseStatistics = courseStatistics,
    monthlyAggregateData = monthlyAggregateData,
    selectedYearMonth = selectedYearMonth,
    isLoading = isLoading,
    isCalendarLoading = isCalendarLoading
)

fun HomeUiState.toUserCourse(): UserCourse = UserCourse(
    id = id,
    name = name,
    goalInHours = goalInHours.toLongOrNull() ?: 0L,
    otherSourceHours = otherSourceHours.toLongOrNull() ?: 0L
)