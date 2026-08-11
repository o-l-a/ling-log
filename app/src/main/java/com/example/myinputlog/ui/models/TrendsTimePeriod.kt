package com.example.myinputlog.ui.models

import androidx.annotation.StringRes
import com.example.myinputlog.R
import java.time.LocalDate
import java.time.ZoneId

enum class TrendsTimePeriod(val dayStep: Int, @get:StringRes val labelRes: Int) {
    LAST_7_DAYS(
        1, R.string.period_last_7_days
    ),
    LAST_4_WEEKS(
        4, R.string.period_last_4_weeks
    ),
    LAST_6_MONTHS(
        14, R.string.period_last_6_months
    ),
    LAST_YEAR(
        32, R.string.period_last_year
    ),
    ALL_TIME(
        28, R.string.period_all_time
    );

    /**
     * Returns a pair of Pair<Long, Long> representing:
     * Current Period (Start to End), Previous Period (Start to End)
     */
    fun getTimeRanges(): Pair<TimeRange, TimeRange> {
        val today = LocalDate.now()

        if (this == ALL_TIME) {
            return TimeRange(
                start = 0L, end = today.toEpochMilli()
            ) to TimeRange(start = 0L, end = 0L)
        }

        val (currentStart, previousStart) = when (this) {
            LAST_7_DAYS -> today.minusDays(6) to today.minusDays(13)
            LAST_4_WEEKS -> today.minusWeeks(4) to today.minusWeeks(8)
            LAST_6_MONTHS -> today.minusMonths(5).firstDayOfMonth() to today.minusMonths(11)
                .firstDayOfMonth()

            LAST_YEAR -> today.minusMonths(11).firstDayOfMonth() to today.minusMonths(23)
                .firstDayOfMonth()
        }

        val currentRange = TimeRange(
            start = currentStart.toEpochMilli(), end = today.toEpochMilli()
        )

        val previousRange = TimeRange(
            start = previousStart.toEpochMilli(), end = currentStart.minusDays(1).toEpochMilli()
        )

        return currentRange to previousRange
    }
}

data class TimeRange(val start: Long, val end: Long)

private fun LocalDate.toEpochMilli(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun LocalDate.firstDayOfMonth(): LocalDate = this.withDayOfMonth(1)