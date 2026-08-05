package com.example.myinputlog.ui.screens.common.composable.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.myinputlog.ui.screens.common.formatDurationAsText
import com.example.myinputlog.ui.screens.common.formatters.RelativeDateFormatter
import com.example.myinputlog.ui.screens.trends.PeriodSummary
import com.example.myinputlog.ui.theme.spacing

@Composable
fun TotalHoursComparisonCard(
    currentPeriodSummary: PeriodSummary,
    previousPeriodSummary: PeriodSummary,
    modifier: Modifier = Modifier,
    isAllTime: Boolean = false
) {
    val formatter = remember { RelativeDateFormatter() }

    val layoutAnimationSpec = tween<Float>(durationMillis = 500, easing = EaseOut)
    val spatialAnimationSpec = tween<IntSize>(durationMillis = 500, easing = EaseOut)

    val previousPeriodWeight by animateFloatAsState(
        targetValue = if (isAllTime) 0.001f else 1f,
        animationSpec = layoutAnimationSpec,
        label = "PreviousPeriodWeight"
    )

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.smallPlus
                )
        ) {
            Column(
                modifier = Modifier
                    .weight(previousPeriodWeight)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = !isAllTime,
                    enter = fadeIn(animationSpec = layoutAnimationSpec),
                    exit = fadeOut(animationSpec = layoutAnimationSpec)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedTextChange(
                            text = formatDurationAsText(previousPeriodSummary.totalSeconds),
                            style = MaterialTheme.typography.headlineSmallEmphasized
                        )
                        PeriodRange(
                            start = formatter.format(previousPeriodSummary.startDate).asString(),
                            end = formatter.format(previousPeriodSummary.endDate).asString()
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = !isAllTime,
                enter = fadeIn(animationSpec = layoutAnimationSpec) + expandHorizontally(
                    animationSpec = spatialAnimationSpec
                ),
                exit = fadeOut(animationSpec = layoutAnimationSpec) + shrinkHorizontally(
                    animationSpec = spatialAnimationSpec
                )
            ) {
                VerticalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedTextChange(
                    text = formatDurationAsText(currentPeriodSummary.totalSeconds),
                    style = MaterialTheme.typography.headlineSmallEmphasized
                )
                PeriodRange(
                    start = formatter.format(currentPeriodSummary.startDate).asString(),
                    end = formatter.format(currentPeriodSummary.endDate, isNaturalText = false)
                        .asString()
                )
            }
        }
    }
}

@Composable
private fun AnimatedTextChange(
    text: String, style: TextStyle, modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = text, transitionSpec = {
            val slideSpec = tween<IntOffset>(durationMillis = 500, easing = EaseOut)
            val fadeSpec = tween<Float>(durationMillis = 500, easing = EaseOut)

            (slideInVertically(animationSpec = slideSpec) { height -> height } + fadeIn(
                animationSpec = fadeSpec
            )).togetherWith(slideOutVertically(animationSpec = slideSpec) { height -> -height } + fadeOut(
                animationSpec = fadeSpec
            )).using(SizeTransform(clip = false))
        }, contentAlignment = Alignment.Center, label = "AnimatedTextChange", modifier = modifier
    ) { targetText ->
        Text(
            text = targetText, style = style, textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PeriodRange(modifier: Modifier = Modifier, start: String, end: String) {
    AnimatedTextChange(
        text = "$start \u2013 $end", style = MaterialTheme.typography.bodySmall, modifier = modifier
    )
}