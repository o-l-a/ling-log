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
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill

private val StartAxisValueFormatter =
    CartesianValueFormatter.decimal(decimalCount = 0, suffix = "%")

@Composable
private fun ComposeCumulativeTrendsChart(
    modelProducer: CartesianChartModelProducer,
    years: List<Long>,
    modifier: Modifier = Modifier,
    dayStep: Int = 1,
    showDayOnAxis: Boolean = true,
    totalDays: Int = 0,
    maxX: Double? = null,
    showMarker: Boolean = true
) {
    val rangeProvider = remember(maxX) { TopSpacedRangeProvider(explicitMaxX = maxX) }
    val baseSpacing = MaterialTheme.spacing.largeTriplePlus
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
    val dynamicMarker = if (showMarker) {
        rememberDynamicMarker(bottomAxisValueFormatter = bottomAxisFormatter)
    } else {
        null
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
                ), rangeProvider = rangeProvider, pointSpacing = dynamicPointSpacing
            ), startAxis = VerticalAxis.rememberStart(
                valueFormatter = StartAxisValueFormatter
            ), bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(style = MaterialTheme.typography.bodySmall),
                valueFormatter = bottomAxisFormatter,
                itemPlacer = dynamicItemPlacer
            ), marker = dynamicMarker, persistentMarkers = { _ ->
                years.forEach { x ->
                    marker at x.toFloat()
                }
            }), modelProducer, modifier, scrollState, rememberVicoZoomState(zoomEnabled = false)
    )
}

@Composable
fun CumulativeTrendsChart(
    progressPoints: List<ProgressPoint>,
    years: List<Long>,
    dayStep: Int,
    modifier: Modifier = Modifier,
    showDayOnAxis: Boolean = true,
    totalDays: Int = 0,
    showMarker: Boolean = true
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val todayEpoch = remember { java.time.LocalDate.now().toEpochDay() }
    val fullMaxX = remember(progressPoints) { progressPoints.lastOrNull()?.date?.toDouble() }

    LaunchedEffect(progressPoints, todayEpoch) {
        val visiblePoints = progressPoints.filter { it.date <= todayEpoch }
        modelProducer.runTransaction {
            lineModel {
                series(visiblePoints.map { it.date }, visiblePoints.map { it.percentageOfGoal })
            }
        }
    }

    ComposeCumulativeTrendsChart(
        modelProducer,
        years,
        modifier,
        dayStep,
        showDayOnAxis,
        totalDays,
        maxX = fullMaxX,
        showMarker = showMarker
    )
}
