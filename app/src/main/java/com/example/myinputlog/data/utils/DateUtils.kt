package com.example.myinputlog.data.utils

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

object DateUtils {
    private val monthKeyFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.systemDefault())

    private val dayKeyFormatter =
        DateTimeFormatter.ofPattern("'day'_d").withZone(ZoneId.systemDefault())

    /**
     * Returns a Date object representing 00:00:00 of the next day.
     */
    fun getStartOfTodayTimestamp(): Date {
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Date.from(startOfDay)
    }

    /**
     * Returns a Date object representing 00:00:00 of the next day.
     */
    fun getStartOfTomorrowTimestamp(): Date {
        val nextDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Date.from(nextDay)
    }

    /**
     * Extension function to convert Date to "YYYY-MM"
     */
    fun Date.toMonthKey(): String = monthKeyFormatter.format(this.toInstant())

    /**
     * Extension function to convert Date to "day_D"
     */
    fun Date.toDayKey(): String = dayKeyFormatter.format(this.toInstant())
}