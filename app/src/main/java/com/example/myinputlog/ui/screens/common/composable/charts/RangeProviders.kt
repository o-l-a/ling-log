package com.example.myinputlog.ui.screens.common.composable.charts

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

/**
 * A Range Provider that adds a proportional 5% space to the top of the chart.
 */
val TopSpacedRangeProvider = object : CartesianLayerRangeProvider {

    override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = minX
    override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = maxX
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = minY
    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val amplitude = maxY - minY
        return if (amplitude < 0.0) {
            maxY + 1.0
        } else {
            maxY + (amplitude * 0.15)
        }
    }
}

val TopSpacedRangeProviderMin0 = object : CartesianLayerRangeProvider {

    override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = minX
    override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = maxX
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = 0.0
    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val amplitude = maxY - minY
        return if (amplitude < 0.0) {
            maxY + 1.0
        } else {
            maxY + (amplitude * 0.15)
        }
    }
}