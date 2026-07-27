package com.example.myinputlog.ui.models

import androidx.annotation.StringRes
import java.time.LocalDate
import java.time.ZoneId

enum class TrendsTimePeriod(val dayStep: Int) {
    LAST_7_DAYS(1),
    LAST_4_WEEKS(4),
    LAST_6_MONTHS(14),
    LAST_YEAR(32),
    ALL_TIME(28);

    /**
     * Returns a pair of Pair<Long, Long> representing:
     * Current Period (Start to End), Previous Period (Start to End)
     */
    fun getTimeRanges(): Pair<TimeRange, TimeRange> {
        val today = LocalDate.now()

        if (this == ALL_TIME) {
            return TimeRange(
                start = 0L,
                end = today.toEpochMilli()
            ) to TimeRange(start = 0L, end = 0L)
        }

        val (currentStart, previousStart) = when (this) {
            LAST_7_DAYS -> today.minusDays(6) to today.minusDays(13)
            LAST_4_WEEKS -> today.minusWeeks(4) to today.minusWeeks(8)
            LAST_6_MONTHS -> today.minusMonths(6) to today.minusMonths(12)
            LAST_YEAR -> today.minusYears(1) to today.minusYears(2)
        }

        val currentRange = TimeRange(
            start = currentStart.toEpochMilli(),
            end = today.toEpochMilli()
        )

        val previousRange = TimeRange(
            start = previousStart.toEpochMilli(),
            end = currentStart.minusDays(1).toEpochMilli()
        )

        return currentRange to previousRange
    }
}

data class TimeRange(val start: Long, val end: Long)

private fun LocalDate.toEpochMilli(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

data class TrendsPeriodOption(
    val period: TrendsTimePeriod,
    @get:StringRes val labelRes: Int
)