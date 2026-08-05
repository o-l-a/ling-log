package com.example.myinputlog.ui.screens.common.composable.stats

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.max

/**
 * A Range Provider that adds a proportional 5% space to the top of the chart.
 */
val TopSpacedRangeProvider = object : CartesianLayerRangeProvider {

    override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = minX
    override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = maxX
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val amplitude = maxY - minY

        if (amplitude < 0.1) {
            val midPoint = (minY + maxY) / 2.0
            return max(midPoint - 0.1, 0.0)
        }
        return minY
    }

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val amplitude = maxY - minY

        if (amplitude < 0.1) {
            val midPoint = (minY + maxY) / 2.0
            return midPoint + 0.1 + (amplitude * 0.1)
        }
        return maxY + (amplitude * 0.1)
    }
}