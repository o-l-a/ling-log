package com.example.myinputlog.ui.screens.common.composable.charts

import android.text.format.DateFormat
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
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis.ItemPlacer.Companion.step
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val StartAxisValueFormatter =
    CartesianValueFormatter.decimal(decimalCount = 0, suffix = "%")

private val BottomAxisValueFormatter = object : CartesianValueFormatter {
    private var currentLocale: Locale? = null
    private var formatter: DateTimeFormatter? = null

    private fun getLocalizedFormatter(): DateTimeFormatter {
        if (formatter == null) {
            val locale = Locale.getDefault()
            val pattern = DateFormat.getBestDateTimePattern(locale, "MMMd")
            formatter = DateTimeFormatter.ofPattern(pattern, locale)
            currentLocale = locale
        }

        return formatter!!
    }

    override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
    ): CharSequence {
        val date = LocalDate.ofEpochDay(value.toLong())
        return date.format(getLocalizedFormatter())
    }
}

@Composable
private fun ComposeCumulativeTrendsChart(
    modelProducer: CartesianChartModelProducer, modifier: Modifier = Modifier, dayStep: Int = 1
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val marker = rememberMarker()
    val scrollState = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
        autoScrollCondition = remember { AutoScrollCondition { _, _ -> true } },
        autoScrollAnimationSpec = remember {
            tween(
                durationMillis = 600, easing = FastOutSlowInEasing
            )
        })

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
            ),
            rangeProvider = TopSpacedRangeProvider,
            pointSpacing = MaterialTheme.spacing.medium
        ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = StartAxisValueFormatter, itemPlacer = remember { step({ 1.0 }) }),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = BottomAxisValueFormatter, itemPlacer = remember {
                    HorizontalAxis.ItemPlacer.aligned(spacing = { dayStep })
                }),
            persistentMarkers = { marker at 20656 }), modelProducer, modifier, scrollState
    )
}

@Composable
fun CumulativeTrendsChart(progressPoints: List<ProgressPoint>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(progressPoints) {
        modelProducer.runTransaction {
            lineModel {
                series(progressPoints.map { it.date }, progressPoints.map { it.percentageOfGoal })
            }
        }
    }
    ComposeCumulativeTrendsChart(modelProducer, modifier)
}
