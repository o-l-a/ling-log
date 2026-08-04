package com.example.myinputlog.ui.screens.common.formatters

import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.common.UiText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class RelativeDateFormatter(
    locale: Locale = Locale.getDefault(), private val today: LocalDate = LocalDate.now()
) {
    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    private val yesterday = today.minusDays(1)
    private val unixEpoch = LocalDate.of(1970, 1, 1)

    fun format(date: LocalDate, isNaturalText: Boolean = true): UiText {
        if (!isNaturalText) return UiText.DynamicString(date.format(formatter))
        return when (date) {
            today -> UiText.StringResource(R.string.today_text)
            yesterday -> UiText.StringResource(R.string.yesterday_text)
            unixEpoch -> UiText.StringResource(R.string.long_ago_text)
            else -> UiText.DynamicString(date.format(formatter))
        }
    }
}