package com.example.myinputlog.ui.screens.common.composable.charts

import android.text.format.DateFormat
import com.example.myinputlog.ui.screens.trends.BucketDescriptor
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import java.time.format.DateTimeFormatter
import java.util.Locale

class GroupedBottomAxisValueFormatter(
    private val descriptors: List<BucketDescriptor>
) : CartesianValueFormatter {
    private var cachedLocale: Locale? = null
    private lateinit var dayFormatter: DateTimeFormatter
    private lateinit var weekFormatter: DateTimeFormatter
    private lateinit var monthFormatter: DateTimeFormatter

    private fun ensureFormatters(locale: Locale) {
        if (cachedLocale == locale) return
        cachedLocale = locale

        dayFormatter =
            DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEE"), locale)
        weekFormatter =
            DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "MMMd"), locale)
        monthFormatter =
            DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "MMM"), locale)
    }

    override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
    ): CharSequence {
        ensureFormatters(Locale.getDefault())

        val index = value.toInt()
        val descriptor = descriptors.getOrNull(index) ?: return "—"

        return when (descriptor) {
            is BucketDescriptor.Day -> descriptor.date.format(dayFormatter)
            is BucketDescriptor.Week -> descriptor.startDate.format(weekFormatter)
            is BucketDescriptor.Month -> descriptor.date.format(monthFormatter)
            is BucketDescriptor.Year -> descriptor.year.toString()
        }
    }
}