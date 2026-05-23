package com.example.myinputlog.ui.screens.home

data class CalendarUiState(
    val monthName: String = "",
    val weekdays: List<String> = listOf(),
    val calendarItems: List<CalendarDay> = listOf(),
    val loadingCalendarItems: List<CalendarDay> = listOf(),
    val today: Int = -1,
    val isLoading: Boolean = true
)
