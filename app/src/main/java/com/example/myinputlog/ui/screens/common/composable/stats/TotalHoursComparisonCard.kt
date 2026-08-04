package com.example.myinputlog.ui.screens.common.composable.stats

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.smallPlus
                )
        ) {
            if (!isAllTime) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        formatDurationAsText(previousPeriodSummary.totalSeconds),
                        style = MaterialTheme.typography.headlineSmallEmphasized
                    )
                    PeriodRange(
                        start = formatter.format(previousPeriodSummary.startDate).asString(),
                        end = formatter.format(previousPeriodSummary.endDate).asString()
                    )
                }
                VerticalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    formatDurationAsText(currentPeriodSummary.totalSeconds),
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
fun PeriodRange(modifier: Modifier = Modifier, start: String, end: String) {
    Text(
        text = "$start \u2013 $end", style = MaterialTheme.typography.bodySmall, modifier = modifier
    )
}