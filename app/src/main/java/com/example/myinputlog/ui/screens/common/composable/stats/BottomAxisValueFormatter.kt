package com.example.myinputlog.ui.screens.common.composable.stats

import android.text.format.DateFormat
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BottomAxisValueFormatter(
    private val showDay: Boolean = true
) : CartesianValueFormatter {
    private var cachedFormatter: Pair<Locale, DateTimeFormatter>? = null

    private fun getFormatter(locale: Locale): DateTimeFormatter {
        val (cachedLocale, formatter) = cachedFormatter ?: (null to null)
        if (cachedLocale == locale && formatter != null) return formatter

        val skeleton = if (showDay) "MMMd" else "MMM"
        val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)

        val newFormatter = DateTimeFormatter.ofPattern(pattern, locale)
        cachedFormatter = locale to newFormatter
        return newFormatter
    }

    override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
    ): CharSequence {
        val date = LocalDate.ofEpochDay(value.toLong())
        return date.format(getFormatter(Locale.getDefault()))
    }
}