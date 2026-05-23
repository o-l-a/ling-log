package com.example.myinputlog.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.CourseTopAppBar
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.utils.DateUtils.toDayKey
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.calendar.SwipeableCalendar
import com.example.myinputlog.ui.screens.utils.formatDurationAsText
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.YearMonth
import java.util.Date
import java.util.concurrent.TimeUnit

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
                        bodyMessage = R.string.empty_course_collection_body
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
                }
            }
        }

        if (homeUiState is HomeUiState.Success && (homeUiState as HomeUiState.Success).isParty) {
            ConfettiOverlay(
                modifier = modifier.fillMaxSize(), stopParty = homeViewModel::confettiStop
            )
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
                    number = formatDurationAsText(homeUiState.courseHeader.totalTimeInSeconds),
                    label = stringResource(R.string.stats_hours_watched),
                    leadingContent = {
                        Icon(imageVector = Icons.Filled.Timer, contentDescription = null)
                    })
                StatisticContainer(
                    modifier = Modifier.weight(1F),
                    number = formatDurationAsText(homeUiState.courseHeader.totalTimeInSecondsToday),
                    label = stringResource(R.string.stats_hours_watched_today),
                    leadingContent = {
                        Icon(imageVector = Icons.Filled.Today, contentDescription = null)
                    })
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
                        Icon(imageVector = Icons.Filled.SmartDisplay, contentDescription = null)
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

@Composable
fun ConfettiOverlay(
    modifier: Modifier, stopParty: () -> Unit
) {
    KonfettiView(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1F), parties = listOf(
            Party(
                speed = 0f,
                maxSpeed = 15f,
                damping = 0.9f,
                angle = Angle.BOTTOM,
                spread = Spread.ROUND,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(100),
                position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0)),
                timeToLive = 3500L
            )
        ), updateListener = object : OnParticleSystemUpdateListener {
            override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                if (activeSystems == 0) stopParty()
            }
        })
}

@Composable
fun StatisticContainer(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    leadingContent: @Composable () -> Unit,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.small),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.6f
            )
        )
    ) {
        Box(modifier = Modifier
            .clickable(enabled = isClickable) { onClick() }
            .fillMaxSize()) {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.default),
                leadingContent = leadingContent,
                headlineContent = {
                    Text(
                        text = number, style = MaterialTheme.typography.labelLarge
                    )
                },
                supportingContent = {
                    if (label.isNotBlank()) {
                        Text(
                            text = label, style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

@Preview
@Composable
fun StatisticContainerPreview() {
    MyInputLogTheme {
        Surface {
            StatisticContainer(
                modifier = Modifier
                    .width(MaterialTheme.spacing.doubleExtraLarge)
                    .height(MaterialTheme.spacing.extraLarge),
                number = "10",
                label = "label",
                leadingContent = {
                    Icon(imageVector = Icons.Filled.Timer, contentDescription = null)
                })
        }
    }
}