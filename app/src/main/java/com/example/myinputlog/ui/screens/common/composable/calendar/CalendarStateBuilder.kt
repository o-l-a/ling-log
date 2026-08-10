package com.example.myinputlog.ui.screens.common.composable.calendar

import com.example.myinputlog.ui.models.DayAggregation
import com.example.myinputlog.ui.models.TopItemsUiModel
import com.example.myinputlog.ui.screens.home.MonthlyStatsResult
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class CalendarDay(
    val dayNumber: String = "",
    val totalMinutes: Long = 0L,
    val text: String = "",
    val isToday: Boolean = false,
    val alpha: Float = 0.2F
)

object CalendarStateBuilder {
    private fun getShortWeekdays(locale: Locale = Locale.getDefault()): List<String> {
        return DayOfWeek.entries.map { dayOfWeek ->
            dayOfWeek.getDisplayName(TextStyle.SHORT, locale).first().toString().uppercase(locale)
        }
    }

    private fun Map<String, DayAggregation>.getCalendarDay(
        dayNumber: Int, today: Int
    ): CalendarDay {
        val dayKey = "day_${dayNumber}"
        val totalMinutes = ((this[dayKey]?.totalTimeInSeconds?.toFloat()?.div(60)) ?: 0F).toLong()
        val text = if (totalMinutes > 0L) "${totalMinutes}m" else ""
        val isToday = dayNumber == today
        val alpha = (totalMinutes.toFloat() / 90).coerceIn(0.2F, 1.0F)
        return CalendarDay(dayNumber.toString(), totalMinutes, text, isToday, alpha)
    }

    fun buildCalendarState(
        monthOnDisplay: YearMonth, monthlyStatsResult: MonthlyStatsResult
    ): CalendarUiState {
        val locale = Locale.getDefault()
        val today = if (monthOnDisplay == YearMonth.now()) LocalDate.now().dayOfMonth else -1
        val monthName = monthOnDisplay.month.getDisplayName(
            TextStyle.FULL_STANDALONE, locale
        ).replaceFirstChar {
            it.titlecase(locale)
        } + " " + monthOnDisplay.year.toString()

        val daysOfWeek = getShortWeekdays()
        val daysOfMonth = monthOnDisplay.lengthOfMonth()
        val firstDayOfWeek = monthOnDisplay.atDay(1).dayOfWeek.value
        val leadingEmptyDays = (firstDayOfWeek - 1 + 7) % 7

        val daysSoFar = leadingEmptyDays + daysOfMonth

        val trailingEmptyDays = if (daysSoFar % 7 != 0) {
            7 - (daysSoFar % 7)
        } else {
            0
        }

        val (monthlyMap, topLabels, topChannels) = when (monthlyStatsResult) {
            is MonthlyStatsResult.Success -> {
                val data = monthlyStatsResult.data
                Triple(data.stats.days, data.topLabels, data.topChannels)
            }

            else -> {
                Triple(
                    emptyMap(),
                    TopItemsUiModel(emptyList(), 0),
                    TopItemsUiModel(emptyList(), 0)
                )
            }
        }

        val isLoading = monthlyStatsResult !is MonthlyStatsResult.Success

        val calendarItems =
            (0 until leadingEmptyDays).map { CalendarDay() } + (1..daysOfMonth).map {
                monthlyMap.getCalendarDay(it, today)
            } + (0 until trailingEmptyDays).map { CalendarDay() }

        val loadingCalendarItems =
            (0 until leadingEmptyDays).map { CalendarDay() } + (1..daysOfMonth).map {
                CalendarDay(dayNumber = it.toString())
            } + (0 until trailingEmptyDays).map { CalendarDay() }

        return CalendarUiState(
            monthName = monthName,
            weekdays = daysOfWeek,
            calendarItems = calendarItems,
            loadingCalendarItems = loadingCalendarItems,
            today = today,
            topLabels = topLabels,
            topChannels = topChannels,
            isLoading = isLoading
        )
    }
}