package com.example.myinputlog.ui.screens.utils.ext

import com.example.myinputlog.ui.screens.utils.dateFormatter
import java.util.Calendar
import java.util.Date

fun Date.formatAsListHeader(): String {
    val calendar = Calendar.getInstance()
    val today = calendar.clone() as Calendar
    val yesterday = calendar.clone() as Calendar

    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    return when {
        isSameDay(today.time, this) -> {
            "Today"
        }

        isSameDay(yesterday.time, this) -> {
            "Yesterday"
        }

        isSameDay(Date(0), this) -> {
            "Long ago"
        }

        else -> {
            dateFormatter.format(this)
        }
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = date1
    cal2.time = date2
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}