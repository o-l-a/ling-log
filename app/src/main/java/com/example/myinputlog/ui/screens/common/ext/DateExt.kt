package com.example.myinputlog.ui.screens.common.ext

import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.common.UiText
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

private val longDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
    .withLocale(Locale.getDefault())

fun getWatchedOnHeader(date: LocalDate): UiText {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val unixEpoch = LocalDate.of(1970, 1, 1)

    return when (date) {
        today -> UiText.StringResource(R.string.today_text)
        yesterday -> UiText.StringResource(R.string.yesterday_text)
        unixEpoch -> UiText.StringResource(R.string.long_ago_text)
        else -> {
            val formatter =
                DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault())
            UiText.DynamicString(date.format(formatter))
        }
    }
}

fun Date.toLocalDate(): LocalDate {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}
