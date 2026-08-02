package com.example.myinputlog.ui.screens.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.TrendsPeriodOption
import com.example.myinputlog.ui.models.TrendsTimePeriod
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogBottomNavBar
import com.example.myinputlog.ui.screens.common.composable.charts.CumulativeTrendsChart
import com.example.myinputlog.ui.screens.common.composable.charts.TrendsDoubleColumnChart
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    modifier: Modifier = Modifier,
    trendsViewModel: TrendsViewModel,
    onBottomNavClicked: (Any) -> Unit,
    navigateToVideoEntry: (String) -> Unit
) {
    val currentCourseId by trendsViewModel.currentCourseId.collectAsStateWithLifecycle()
    val trendsUiState by trendsViewModel.trendsUiState.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {},
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Trends,
                onBottomNavClicked = onBottomNavClicked,
                navigateToVideoEntry = { navigateToVideoEntry(currentCourseId) })
        }) { innerPadding ->
        when (val currentState = trendsUiState) {
            TrendsUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            TrendsUiState.Loading -> {
                LoadingBox()
            }

            is TrendsUiState.Success -> TrendsBody(
                modifier = Modifier.padding(innerPadding),
                trendsUiState = currentState,
                periodOptions = trendsViewModel.timePeriodOptions,
                onPeriodChange = trendsViewModel::setTimePeriod,
                onChannelLimitChange = trendsViewModel::expandChannels
            )
        }
    }
}

@Composable
fun TrendsBody(
    modifier: Modifier = Modifier,
    trendsUiState: TrendsUiState.Success,
    periodOptions: List<TrendsPeriodOption>,
    onPeriodChange: (period: TrendsTimePeriod) -> Unit,
    onChannelLimitChange: () -> Unit
) {
    val scrollState = rememberLazyListState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(MaterialTheme.spacing.mediumPlus),
        state = scrollState
    ) {
        item {
            PeriodSection(
                periodOptions = periodOptions,
                selectedPeriod = trendsUiState.selectedPeriod,
                onPeriodChange = onPeriodChange
            )
        }
        item {
            if (trendsUiState.cumulativeProgress.isNotEmpty()) {
                Text(
                    stringResource(
                        R.string.trends_progress_chart_title
                    ), style = MaterialTheme.typography.bodyLarge
                )
                CumulativeTrendsChart(
                    trendsUiState.cumulativeProgress,
                    trendsUiState.years,
                    trendsUiState.selectedPeriod.dayStep,
                    modifier = Modifier.height(MaterialTheme.spacing.horizontalChartHeight),
                    trendsUiState.selectedPeriod.dayStep < 28,
                    trendsUiState.totalPoints
                )
            }
        }
        item {
            Text(
                stringResource(
                    R.string.trends_progress_chart_title
                ), style = MaterialTheme.typography.bodyLarge
            )
            TrendsDoubleColumnChart(
                trendsUiState.chartBucketData,
                Modifier.height(MaterialTheme.spacing.horizontalChartHeight),
                isAllTime = trendsUiState.selectedPeriod == TrendsTimePeriod.ALL_TIME
            )
        }
        item {
            Text(trendsUiState.regionStats.toString())
        }
        item {
            Text(trendsUiState.topLabels.toString())
        }
        item {
            Text(trendsUiState.topChannels.toString())
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PeriodSection(
    modifier: Modifier = Modifier,
    periodOptions: List<TrendsPeriodOption>,
    selectedPeriod: TrendsTimePeriod,
    onPeriodChange: (period: TrendsTimePeriod) -> Unit
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(periodOptions) { option ->
            val isSelected = selectedPeriod == option.period

            FilterChip(
                selected = isSelected, onClick = { onPeriodChange(option.period) }, label = {
                Text(
                    text = stringResource(option.labelRes)
                )
            }, leadingIcon = if (isSelected) {
                {
                    Icon(
                        imageVector = Icons.Default.Check, contentDescription = null
                    )
                }
            } else null)
        }
    }
}