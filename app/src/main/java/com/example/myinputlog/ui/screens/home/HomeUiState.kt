package com.example.myinputlog.ui.screens.home

import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import java.time.YearMonth

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object NetworkError : HomeUiState
    data class Success(
        val currentCourse: UserCourse = UserCourse(),

        val courseStatistics: CourseStatistics = CourseStatistics(),
        val selectedYearMonth: YearMonth = YearMonth.now(),
        val monthlyAggregateData: List<Long> = listOf(),

        val userCourses: List<UserCourse> = listOf(),
        val isCalendarLoading: Boolean = true,
        val isParty: Boolean = false
    ) : HomeUiState
}