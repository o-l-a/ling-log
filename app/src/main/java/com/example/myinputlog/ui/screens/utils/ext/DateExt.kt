package com.example.myinputlog.ui.screens.utils.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.dateFormatter
import java.util.Calendar
import java.util.Date

@Composable
fun Date.formatAsListHeader(): String {
    val calendar = Calendar.getInstance()
    val today = calendar.clone() as Calendar
    val yesterday = calendar.clone() as Calendar

    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    return when {
        isSameDay(today.time, this) -> {
            stringResource(R.string.today_text)
        }

        isSameDay(yesterday.time, this) -> {
            stringResource(R.string.yesterday_text)
        }

        isSameDay(Date(0), this) -> {
            stringResource(R.string.long_ago_text)
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

fun Date.asStartOfDay(): Date {
    val calendar = Calendar.getInstance()
    this.let { date ->
        calendar.time = date
    }

    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.time
}
