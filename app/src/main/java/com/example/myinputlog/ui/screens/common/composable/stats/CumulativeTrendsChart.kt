package com.example.myinputlog.ui.screens.common.composable.stats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.myinputlog.ui.screens.trends.ProgressPoint
import com.example.myinputlog.ui.theme.spacing
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill

import java.math.RoundingMode
import java.text.NumberFormat

private val axisFormatters = Array(4) { decimals ->
    NumberFormat.getNumberInstance().apply {
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = true
        maximumFractionDigits = decimals
        minimumFractionDigits = decimals
    }
}

private val StartAxisValueFormatter =
    CartesianValueFormatter { context, value, verticalAxisPosition ->
        val position = verticalAxisPosition ?: Axis.Position.Vertical.Start
        val bounds = context.ranges.getYRange(position)
        val yRange = bounds.maxY - bounds.minY

        val decimalPlaces = when {
            yRange < 0.1 -> 3
            yRange < 1.0 -> 2
            yRange < 10.0 -> 1
            else -> 0
        }
        "${axisFormatters[decimalPlaces].format(value)}%"
    }

@Composable
private fun ComposeCumulativeTrendsChart(
    modelProducer: CartesianChartModelProducer,
    years: List<Long>,
    modifier: Modifier = Modifier,
    dayStep: Int = 1,
    showDayOnAxis: Boolean = true,
    totalDays: Int = 0
) {
    val baseSpacing = MaterialTheme.spacing.largePlusPlus
    val lineColor = MaterialTheme.colorScheme.primary
    val marker = rememberMarker()
    val scrollState = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
        autoScrollCondition = remember { AutoScrollCondition { _, _ -> true } },
        autoScrollAnimationSpec = remember {
            tween(
                durationMillis = 800, easing = FastOutSlowInEasing
            )
        })
    val dynamicPointSpacing = remember(dayStep, totalDays, showDayOnAxis) {
        val divisor = if (showDayOnAxis) {
            dayStep.toFloat()
        } else {
            calculateMonthStep(totalDays.toDouble()) * 30f
        }
        baseSpacing / divisor.coerceAtLeast(1f)
    }
    val dynamicItemPlacer = remember(dayStep, showDayOnAxis) {
        if (showDayOnAxis) {
            HorizontalAxis.ItemPlacer.aligned(spacing = { dayStep })
        } else {
            MonthItemPlacer()
        }
    }
    val bottomAxisFormatter = remember(showDayOnAxis) {
        BottomAxisValueFormatter(showDay = showDayOnAxis)
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(
                                Brush.verticalGradient(
                                    listOf(
                                        lineColor.copy(alpha = 0.4f), Color.Transparent
                                    )
                                )
                            )
                        ),
                        interpolator = LineCartesianLayer.Interpolator.cubic(),
                    )
                ), rangeProvider = TopSpacedRangeProvider, pointSpacing = dynamicPointSpacing
            ), startAxis = VerticalAxis.rememberStart(
                valueFormatter = StartAxisValueFormatter
            ), bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(style = MaterialTheme.typography.bodySmall),
                valueFormatter = bottomAxisFormatter,
                itemPlacer = dynamicItemPlacer
            ), persistentMarkers = { _ ->
                years.forEach { x ->
                    marker at x.toFloat()
                }
            }), modelProducer, modifier, scrollState
    )
}

@Composable
fun CumulativeTrendsChart(
    progressPoints: List<ProgressPoint>,
    years: List<Long>,
    dayStep: Int,
    modifier: Modifier = Modifier,
    showDayOnAxis: Boolean = true,
    totalDays: Int = 0
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(progressPoints) {
        modelProducer.runTransaction {
            lineModel {
                series(progressPoints.map { it.date }, progressPoints.map { it.percentageOfGoal })
            }
        }
    }
    ComposeCumulativeTrendsChart(
        modelProducer, years, modifier, dayStep, showDayOnAxis, totalDays
    )
}
