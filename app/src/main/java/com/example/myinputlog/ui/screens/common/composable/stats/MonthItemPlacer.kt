package com.example.myinputlog.ui.screens.common.composable.stats

import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import java.time.LocalDate

fun calculateMonthStep(totalDays: Double): Int {
    val totalMonths = totalDays / 30.0
    return when {
        totalMonths <= 13 -> 1
        totalMonths <= 24 -> 2
        totalMonths <= 48 -> 3
        totalMonths <= 72 -> 6
        else -> 12
    }
}

class MonthItemPlacer(
    private val basePlacer: HorizontalAxis.ItemPlacer = HorizontalAxis.ItemPlacer.aligned(
        addExtremeLabelPadding = true
    )
) : HorizontalAxis.ItemPlacer by basePlacer {

    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float
    ): List<Double> {
        val labels = mutableListOf<Double>()

        val totalDays = fullXRange.endInclusive - fullXRange.start
        val monthStep = calculateMonthStep(totalDays)

        var current = LocalDate.ofEpochDay(fullXRange.start.toLong()).withDayOfMonth(1)

        val monthRemainder = (current.monthValue - 1) % monthStep
        if (monthRemainder != 0) {
            current = current.plusMonths((monthStep - monthRemainder).toLong())
        }

        val dataEnd = LocalDate.ofEpochDay(fullXRange.endInclusive.toLong())

        while (!current.isAfter(dataEnd)) {
            labels.add(current.toEpochDay().toDouble())
            current = current.plusMonths(monthStep.toLong())
        }

        return labels
    }

    override fun getLineValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float
    ): List<Double> {
        return getLabelValues(context, visibleXRange, fullXRange, maxLabelWidth)
    }
}