package com.example.myinputlog.ui.screens.common.composable.stats

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * A Range Provider that adds a proportional 5% space to the top of the chart.
 */
class TopSpacedRangeProvider(
    private val explicitMaxX: Double? = null,
) : CartesianLayerRangeProvider {
    override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = minX
    override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore): Double =
        explicitMaxX ?: maxX

    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val amplitude = maxY - minY

        return if (amplitude < 2.0) {
            val midPoint = (minY + maxY) / 2.0
            max(floor(midPoint - 0.5), 0.0)
        } else {
            floor(minY)
        }
    }

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val amplitude = maxY - minY

        return if (amplitude < 2.0) {
            val midPoint = (minY + maxY) / 2.0
            ceil(midPoint + 1.0)
        } else {
            ceil(maxY + (amplitude * 0.1))
        }
    }
}