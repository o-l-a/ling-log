package com.example.myinputlog.ui.screens.common.composable.charts

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.screens.trends.TrendsUiState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill

private val RangeProvider = CartesianLayerRangeProvider.Intrinsic
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(suffix = "%")
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(suffix = "%")

@Composable
private fun ComposeElectricCarSales(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider =
                    LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                            areaFill =
                                LineCartesianLayer.AreaFill.single(
                                    Fill(
                                        Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.4f), Color.Transparent))
                                    )
                                ),
                            interpolator = LineCartesianLayer.Interpolator.catmullRom(),
                        )
                    ),
                rangeProvider = RangeProvider,
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = StartAxisValueFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(),
            marker = rememberMarker(MarkerValueFormatter),
        ),
        modelProducer,
        modifier.height(216.dp),
        rememberVicoScrollState(scrollEnabled = false),
    )
}

@Composable
fun ComposeCarSales(x: List<Long>, y :List<Float>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineModel { series(x, y) }
        }
    }
    ComposeElectricCarSales(modelProducer, modifier)
}

@Composable
fun CumulativeTrendsChart(
    successState: TrendsUiState.Success, modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(successState.cumulativeProgress) {
        modelProducer.runTransaction {
            lineModel {
                series(successState.cumulativeProgress.map { item -> item.percentageOfGoal })
            }
        }
    }

    val bottomAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        successState.cumulativeProgress.getOrNull(value.toInt())?.dateString ?: ""
    }

    val startAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        "${(value * 100).toInt()}%"
    }

    CartesianChartHost(
        modifier = modifier, modelProducer = modelProducer, chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.Line(
                        fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.primary)),
                        stroke = LineCartesianLayer.LineStroke.Continuous()
                    )
                )
            ), startAxis = VerticalAxis.rememberStart(
                valueFormatter = startAxisValueFormatter
            ), bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisValueFormatter, labelRotationDegrees = 45f
            )
        )
    )
}