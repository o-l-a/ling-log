package com.example.myinputlog.ui.screens.common.composable.charts

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.screens.common.formatDurationAsText
import com.example.myinputlog.ui.screens.trends.ChartBucketData
import com.example.myinputlog.ui.theme.spacing
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent

private val StartAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
    val seconds = value.toLong()
    formatDurationAsText(seconds)
}

@Composable
fun TrendsDoubleColumnChart(
    chartBucketData: ChartBucketData, modifier: Modifier = Modifier, isAllTime: Boolean = false
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val maxSeconds =
        remember(chartBucketData.currentProgress, chartBucketData.previousProgress, isAllTime) {
            maxOf(chartBucketData.currentProgress.maxOfOrNull { it.toFloat() } ?: 0f,
                if (isAllTime) 0f else (chartBucketData.previousProgress.maxOfOrNull { it.toFloat() }
                    ?: 0f))
        }

    val stubValue = remember(maxSeconds) {
        if (maxSeconds > 0f) maxSeconds * 0.01f else 60f
    }

    val currentProgress = remember(chartBucketData.currentProgress) {
        chartBucketData.currentProgress.map { if (it <= 0f) stubValue else it }
    }
    val previousProgress = remember(chartBucketData.previousProgress) {
        chartBucketData.previousProgress.map { if (it <= 0f) stubValue else it }
    }

    LaunchedEffect(chartBucketData.currentProgress, chartBucketData.previousProgress) {
        modelProducer.runTransaction {
            columnModel {
                if (!isAllTime) {
                    series(previousProgress)
                }
                series(currentProgress)
            }
        }
    }

    val bottomAxisFormatter = remember(chartBucketData.descriptors) {
        GroupedBottomAxisValueFormatter(chartBucketData.descriptors)
    }

    val startAxisItemPlacer = remember(maxSeconds) {
        VerticalAxis.ItemPlacer.step(
            step = { if (maxSeconds > 3600f) 3600.0 else null })
    }

    val scrollState = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
        autoScrollCondition = remember { AutoScrollCondition { _, _ -> true } },
        autoScrollAnimationSpec = remember {
            tween(
                durationMillis = 800, easing = FastOutSlowInEasing
            )
        })

    val primaryLineComponent = rememberLineComponent(
        fill = Fill(MaterialTheme.colorScheme.primary), thickness = MaterialTheme.spacing.medium
    )
    val secondaryLineComponent = rememberLineComponent(
        fill = Fill(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)),
        thickness = MaterialTheme.spacing.medium
    )

    val columnProvider = remember(isAllTime) {
        if (isAllTime) {
            ColumnCartesianLayer.ColumnProvider.series(primaryLineComponent)
        } else {
            ColumnCartesianLayer.ColumnProvider.series(secondaryLineComponent, primaryLineComponent)
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
        columnProvider = columnProvider,
        rangeProvider = TopSpacedRangeProviderMin0,
        columnCollectionSpacing = MaterialTheme.spacing.large,
        mergeMode = remember { { ColumnCartesianLayer.MergeMode.Grouped(columnSpacing = 0.dp) } }),
        startAxis = VerticalAxis.rememberStart(
            valueFormatter = StartAxisValueFormatter, itemPlacer = startAxisItemPlacer
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = bottomAxisFormatter, guideline = null
        )), modelProducer = modelProducer, modifier = modifier, scrollState = scrollState)
}