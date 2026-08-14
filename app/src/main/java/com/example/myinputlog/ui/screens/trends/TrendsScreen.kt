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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.RankingCategory
import com.example.myinputlog.ui.models.RankingLimit
import com.example.myinputlog.ui.models.TrendsTimePeriod
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogBottomNavBar
import com.example.myinputlog.ui.screens.common.composable.bars.TrendsScreenTopAppBar
import com.example.myinputlog.ui.screens.common.composable.channel.ChannelRepresentation
import com.example.myinputlog.ui.screens.common.composable.country.CountryRepresentation
import com.example.myinputlog.ui.screens.common.composable.input.FilterDropdownChip
import com.example.myinputlog.ui.screens.common.composable.label.ClickableLabelChip
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.screens.common.composable.stats.CumulativeTrendsChart
import com.example.myinputlog.ui.screens.common.composable.stats.RankingContributorListItem
import com.example.myinputlog.ui.screens.common.composable.stats.RankingListItem
import com.example.myinputlog.ui.screens.common.composable.stats.TotalHoursComparisonCard
import com.example.myinputlog.ui.screens.common.formatDurationAsText
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
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            when (val currentUiState = trendsUiState) {
                is TrendsUiState.Content -> {
                    TrendsScreenTopAppBar(
                        scrollBehavior = scrollBehavior,
                        periodOptions = TrendsTimePeriod.entries,
                        selectedPeriod = currentUiState.selectedPeriod,
                        onPeriodChange = trendsViewModel::setTimePeriod
                    )
                }

                else -> {}
            }
        },
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
    onCategoryChange: (RankingCategory) -> Unit,
    onCategoryLimitChange: (RankingLimit) -> Unit
) {
    val scrollState = rememberLazyListState()

    var expandedLabelId by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedCountry by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(MaterialTheme.spacing.mediumPlus),
        state = scrollState
    ) {
//        item {
//            PeriodSection(
//                periodOptions = periodOptions,
//                selectedPeriod = trendsUiState.selectedPeriod,
//                onPeriodChange = onPeriodChange
//            )
//        }
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
            RankingFilterSection(
                trendsUiState.selectedRankingCategory,
                onCategoryChange,
                trendsUiState.selectedRankingLimit,
                onCategoryLimitChange
            )
        }
        when (trendsUiState) {
            is TrendsUiState.Success -> {
                when (trendsUiState.selectedRankingCategory) {
                    RankingCategory.LABEL -> {
                        trendsUiState.topLabels.forEachIndexed { index, label ->
                            item(key = "label_${label.id}") {
                                RankingListItem(
                                    rankIndex = index + 1,
                                    durationText = formatDurationAsText(label.totalSeconds),
                                    isExpandable = trendsUiState.topChannels.isNotEmpty(),
                                    isExpanded = expandedLabelId == label.id,
                                    onClick = {
                                        expandedLabelId =
                                            if (expandedLabelId == label.id) null else label.id
                                    },
                                    modifier = Modifier.animateItem(),
                                    representation = {
                                        ClickableLabelChip(
                                            onClick = {},
                                            title = label.title,
                                            backgroundColor = Color(label.color),
                                            textColor = Color(label.textColor),
                                        )
                                    })
                            }

                            if (expandedLabelId == label.id) {
                                items(
                                    items = label.channelBreakdown,
                                    key = { "label_sub_${label.id}_${it.channelId}" }) { channel ->
                                    RankingContributorListItem(
                                        durationText = formatDurationAsText(channel.totalSeconds),
                                        modifier = Modifier.animateItem(),
                                        representation = {
                                            ChannelRepresentation(
                                                channel.channelName, channel.thumbnailMediumUrl
                                            )
                                        })
                                }
                            }
                        }
                    }

                    RankingCategory.CHANNEL -> {
                        trendsUiState.topChannels.forEachIndexed { index, channel ->
                            item(key = "channel_${channel.id}") {
                                RankingListItem(
                                    rankIndex = index + 1,
                                    durationText = formatDurationAsText(channel.totalTimeInSeconds),
                                    isExpandable = false,
                                    isExpanded = false,
                                    onClick = {},
                                    modifier = Modifier.animateItem(),
                                    representation = {
                                        ChannelRepresentation(
                                            channel.title, channel.thumbnailMediumUrl
                                        )
                                    })
                            }
                        }
                    }

                    RankingCategory.COUNTRY -> {
                        trendsUiState.regionStats.forEachIndexed { index, country ->
                            item(key = "country_${country.isoCode}") {
                                RankingListItem(
                                    rankIndex = index + 1,
                                    durationText = formatDurationAsText(country.totalSeconds),
                                    isExpandable = trendsUiState.topChannels.isNotEmpty(),
                                    isExpanded = expandedCountry == country.isoCode,
                                    onClick = {
                                        expandedCountry =
                                            if (expandedCountry == country.isoCode) null else country.isoCode
                                    },
                                    modifier = Modifier.animateItem(),
                                    representation = {
                                        CountryRepresentation(country)
                                    })
                            }

                            if (expandedCountry == country.isoCode) {
                                items(
                                    items = country.channelBreakdown,
                                    key = { "country_sub_${country.isoCode}_${it.channelId}" }) { channel ->
                                    RankingContributorListItem(
                                        durationText = formatDurationAsText(channel.totalSeconds),
                                        modifier = Modifier.animateItem(),
                                        representation = {
                                            ChannelRepresentation(
                                                channel.channelName, channel.thumbnailMediumUrl
                                            )
                                        })
                                }
                            }
                        }
                    }
                }
            }

            is TrendsUiState.Empty -> {
                item("emptyCategory") {
                    EmptyCollectionBox(bodyMessage = R.string.empty_stats_collection_body)
                }
            }
        }
    }
}

@Composable
fun RankingFilterSection(
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