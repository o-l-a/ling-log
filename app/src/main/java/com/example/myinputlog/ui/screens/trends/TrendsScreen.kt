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
import com.example.myinputlog.ui.models.RankingCategory
import com.example.myinputlog.ui.models.RankingLimit
import com.example.myinputlog.ui.models.TrendsTimePeriod
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogBottomNavBar
import com.example.myinputlog.ui.screens.common.composable.input.FilterDropdownChip
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.screens.common.composable.stats.CumulativeTrendsChart
import com.example.myinputlog.ui.screens.common.composable.stats.TotalHoursComparisonCard
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

            is TrendsUiState.Content -> TrendsBody(
                modifier = Modifier.padding(innerPadding),
                trendsUiState = currentState,
                periodOptions = TrendsTimePeriod.entries,
                onPeriodChange = trendsViewModel::setTimePeriod,
                onCategoryChange = trendsViewModel::setRankingCategory,
                onCategoryLimitChange = trendsViewModel::setRankingLimit
            )
        }
    }
}

@Composable
fun TrendsBody(
    modifier: Modifier = Modifier,
    trendsUiState: TrendsUiState.Content,
    periodOptions: List<TrendsTimePeriod>,
    onPeriodChange: (period: TrendsTimePeriod) -> Unit,
    onCategoryChange: (RankingCategory) -> Unit,
    onCategoryLimitChange: (RankingLimit) -> Unit
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
        when (trendsUiState) {
            is TrendsUiState.Success -> {
                item(key = "totalProgress") {
                    Text(
                        stringResource(
                            R.string.trends_progress_chart_title
                        ), style = MaterialTheme.typography.bodyLarge
                    )
                    CumulativeTrendsChart(
                        trendsUiState.cumulativeProgress,
                        trendsUiState.years,
                        trendsUiState.selectedPeriod.dayStep,
                        modifier = Modifier
                            .height(MaterialTheme.spacing.horizontalChartHeight)
                            .animateItem(),
                        trendsUiState.selectedPeriod.dayStep < 28,
                        trendsUiState.totalPoints
                    )
                }
                item(key = "progressCard") {
                    TotalHoursComparisonCard(
                        trendsUiState.currentPeriodSummary,
                        trendsUiState.previousPeriodSummary,
                        Modifier
                            .height(MaterialTheme.spacing.extraLargePlusPlus)
                            .animateItem(),
                        isAllTime = trendsUiState.selectedPeriod == TrendsTimePeriod.ALL_TIME
                    )
                }
            }

            is TrendsUiState.Empty -> {
                item("emptyProgress") {
                    EmptyCollectionBox(bodyMessage = R.string.empty_stats_collection_body)
                }
            }
        }
        item {
            RankingFilterRow(
                trendsUiState.selectedRankingCategory,
                onCategoryChange,
                trendsUiState.selectedRankingLimit,
                onCategoryLimitChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PeriodSection(
    modifier: Modifier = Modifier,
    periodOptions: List<TrendsTimePeriod>,
    selectedPeriod: TrendsTimePeriod,
    onPeriodChange: (period: TrendsTimePeriod) -> Unit
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(periodOptions) { option ->
            val isSelected = selectedPeriod == option

            FilterChip(
                selected = isSelected, onClick = { onPeriodChange(option) }, label = {
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

@Composable
fun RankingFilterRow(
    selectedCategory: RankingCategory,
    onCategoryChanged: (RankingCategory) -> Unit,
    selectedLimit: RankingLimit,
    onLimitChanged: (RankingLimit) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterDropdownChip(
                selectedItem = selectedCategory,
                items = RankingCategory.entries,
                itemLabelMapper = { stringResource(id = it.labelRes) },
                onItemSelected = onCategoryChanged,
            )
        }

        item {
            FilterDropdownChip(
                selectedItem = selectedLimit,
                items = RankingLimit.entries,
                itemLabelMapper = { "Top ${it.limit}" },
                onItemSelected = onLimitChanged
            )
        }
    }
}