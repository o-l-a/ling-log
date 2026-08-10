package com.example.myinputlog.ui.screens.common.composable.calendar

import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.TopItemsUiModel

data class CalendarUiState(
    val monthName: String = "",
    val weekdays: List<String> = listOf(),
    val calendarItems: List<CalendarDay> = listOf(),
    val loadingCalendarItems: List<CalendarDay> = listOf(),
    val today: Int = -1,
    val topLabels: TopItemsUiModel<LabelUiModel>,
    val topChannels: TopItemsUiModel<ChannelUiModel>,
    val isLoading: Boolean = true
)