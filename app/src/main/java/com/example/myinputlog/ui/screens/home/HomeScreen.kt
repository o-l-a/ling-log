package com.example.myinputlog.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.CourseTopAppBar
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.utils.DateUtils.toDayKey
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.ConfettiOverlay
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.SpinningClockIcon
import com.example.myinputlog.ui.screens.utils.composable.StatisticContainer
import com.example.myinputlog.ui.screens.utils.composable.calendar.SwipeableCalendar
import com.example.myinputlog.ui.screens.utils.formatDurationAsText
import com.example.myinputlog.ui.theme.spacing
import java.time.YearMonth
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    onBottomNavClicked: (Any) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
) {
    val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val monthlyStatsMap by homeViewModel.monthlyStatsMap.collectAsStateWithLifecycle()
    val currentCourseId by homeViewModel.currentCourseId.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            if (homeUiState is HomeUiState.Success) {
                CourseTopAppBar(
                    courseHeader = (homeUiState as HomeUiState.Success).courseHeader,
                    onValueChange = homeViewModel::changeCurrentCourseId,
                    options = (homeUiState as HomeUiState.Success).userCourses,
                    scrollBehavior = scrollBehavior,
                )
            }
        }, bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Home,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(currentCourseId) })
        }) { innerPadding ->
            when (val state = homeUiState) {
                is HomeUiState.Loading -> {
                    LoadingBox()
                }

                is HomeUiState.Empty -> {
                    EmptyCollectionBox(
                        modifier = modifier.padding(MaterialTheme.spacing.medium),
                        bodyMessage = R.string.empty_course_collection_body_other_tabs
                    )
                }

                is HomeUiState.Error -> {
                    EmptyCollectionBox(
                        modifier = modifier.padding(MaterialTheme.spacing.medium),
                        bodyMessage = R.string.something_went_wrong
                    )
                }

                is HomeUiState.NetworkError -> {
                    EmptyCollectionBox(
                        modifier = Modifier.padding(top = MaterialTheme.spacing.doubleExtraLarge),
                        bodyMessage = R.string.stats_network_error
                    )
                }

                is HomeUiState.Success -> {
                    val todaySeconds = remember(monthlyStatsMap) {
                        val stats = (monthlyStatsMap[YearMonth.now()
                            .toString()] as? MonthlyStatsResult.Success)?.data
                        stats?.days?.get(Date().toDayKey())?.totalTimeInSeconds ?: 0L
                    }
                    val updatedSuccessState = state.copy(
                        courseHeader = state.courseHeader.copy(totalTimeInSecondsToday = todaySeconds)
                    )
                    HomeBody(
                        modifier = modifier.padding(innerPadding),
                        homeUiState = updatedSuccessState,
                        monthlyStatsMap = monthlyStatsMap,
                        onMonthSettled = homeViewModel::onMonthSettled,
                        doParty = homeViewModel::confetti
                    )
                    if (state.isParty) {
                        val confettiIntColors = remember(state.confettiColors) {
                            state.confettiColors.map { it.toInt() }
                        }
                        ConfettiOverlay(
                            modifier = modifier.fillMaxSize(),
                            stopParty = homeViewModel::confettiStop,
                            colors = confettiIntColors
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeBody(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState.Success,
    monthlyStatsMap: Map<String, MonthlyStatsResult>,
    onMonthSettled: (YearMonth) -> Unit,
    doParty: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val isScrollEnabled by remember {
        derivedStateOf {
            scrollState.canScrollForward || scrollState.canScrollBackward
        }
    }
    var clockSpinTrigger by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        contentPadding = PaddingValues(MaterialTheme.spacing.extraSmall),
        state = scrollState,
        userScrollEnabled = isScrollEnabled
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                StatisticContainer(
                    modifier = Modifier.weight(1F),
                    number = homeUiState.courseHeader.totalActiveDays,
                    label = stringResource(R.string.stats_total_days),
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.img_emoji_fire),
                            contentDescription = "Calendar",
                            modifier = Modifier.size(MaterialTheme.spacing.statIconSize),
                            colorFilter = if (homeUiState.courseHeader.totalTimeInSecondsToday == 0L) ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0f) }) else null
                        )
                    })
                StatisticContainer(
                    modifier = Modifier.weight(1F),
                    number = formatDurationAsText(homeUiState.courseHeader.dailyAverageSeconds),
                    label = stringResource(R.string.stats_daily_average),
                    leadingContent = {
                        SpinningClockIcon(
                            spinTrigger = clockSpinTrigger,
                            modifier = Modifier.size(MaterialTheme.spacing.statIconSize)
                        )
                    },
                    isClickable = true,
                    onClick = { clockSpinTrigger++ })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                StatisticContainer(
                    modifier = Modifier.weight(1F),
                    number = homeUiState.courseHeader.videoCount.toString(),
                    label = stringResource(R.string.stats_videos_watched),
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.img_emoji_clapper),
                            contentDescription = "Clapper",
                            modifier = Modifier.size(MaterialTheme.spacing.statIconSize)
                        )
                    })
                StatisticContainer(
                    modifier = Modifier.weight(1F),
                    number = stringResource(R.string.yay),
                    label = stringResource(R.string.yay),
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.img_emoji_celebration),
                            contentDescription = "Celebration",
                            modifier = Modifier.size(MaterialTheme.spacing.large)
                        )
                    },
                    isClickable = true,
                    onClick = doParty
                )
            }
        }
        item {
            SwipeableCalendar(
                selectedCourseId = homeUiState.courseHeader.id,
                monthlyStatsMap = monthlyStatsMap,
                onMonthSettled = onMonthSettled,
            )
        }
    }
}