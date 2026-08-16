package com.example.myinputlog.ui.models

import androidx.annotation.StringRes
import com.example.myinputlog.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

enum class TrendsTimePeriod(val dayStep: Int, @get:StringRes val labelRes: Int) {
    LAST_7_DAYS(1, R.string.period_last_7_days), LAST_4_WEEKS(
        4,
        R.string.period_last_4_weeks
    ),
    LAST_6_MONTHS(14, R.string.period_last_6_months), LAST_YEAR(
        32,
        R.string.period_last_year
    ),
    ALL_TIME(28, R.string.period_all_time), CUSTOM_MONTH(3, R.string.period_custom_month);

    /**
     * Returns a pair of Pair<Long, Long> representing:
     * Current Period (Start to End), Previous Period (Start to End)
     */
    fun getTimeRanges(month: YearMonth?): Pair<TimeRange, TimeRange> {
        val today = LocalDate.now()
        val customMonth = month ?: YearMonth.now()

        if (this == ALL_TIME) {
            return TimeRange(
                start = 0L, end = today.toEndOfDayEpochMilli()
            ) to TimeRange(start = 0L, end = 0L)
        }

        val (currentStart, previousStart) = when (this) {
            LAST_7_DAYS -> today.minusDays(6) to today.minusDays(13)
            LAST_4_WEEKS -> today.minusWeeks(4) to today.minusWeeks(8)
            LAST_6_MONTHS -> today.minusMonths(5).firstDayOfMonth() to today.minusMonths(11)
                .firstDayOfMonth()

            LAST_YEAR -> today.minusMonths(11).firstDayOfMonth() to today.minusMonths(23)
                .firstDayOfMonth()

            CUSTOM_MONTH -> customMonth.atDay(1) to customMonth.minusMonths(1)?.atDay(1)
        }

        val currentRange = when (this) {
            CUSTOM_MONTH -> TimeRange(
                start = currentStart.toStartOfDayEpochMilli(),
                end = customMonth.atEndOfMonth().toEndOfDayEpochMilli()
            )

            else -> TimeRange(
                start = currentStart.toStartOfDayEpochMilli(), end = today.toEndOfDayEpochMilli()
            )
        }

        val previousRange = TimeRange(
            start = previousStart.toStartOfDayEpochMilli(),
            end = currentStart.minusDays(1).toEndOfDayEpochMilli()
        )

        return currentRange to previousRange
    }
}

data class TimeRange(val start: Long, val end: Long)

fun LocalDate.toStartOfDayEpochMilli(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun LocalDate.toEndOfDayEpochMilli(): Long {
    return this.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun LocalDate.firstDayOfMonth(): LocalDate = this.withDayOfMonth(1)