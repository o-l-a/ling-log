package com.example.myinputlog.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.CourseTopAppBar
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.MyInputLogCalendar
import com.example.myinputlog.ui.screens.utils.formatDurationAsText
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

object HomeDestination : NavigationDestination {
    override val route: String = "home"
    override val titleRes: Int = R.string.home_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
) {
    val homeUiState = homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val userCourses = homeUiState.value.userCourses.collectAsStateWithLifecycle(emptyList())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (userCourses.value != null) {
                CourseTopAppBar(
                    course = homeUiState.value.toUserCourse(),
                    courseStatistics = homeUiState.value.courseStatistics,
                    onValueChange = homeViewModel::changeCurrentCourseId,
                    options = userCourses.value!!,
                    scrollBehavior = scrollBehavior
                )
            }
        },
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Home,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(homeUiState.value.id) }
            )
        }
    ) { innerPadding ->
        if (userCourses.value == null) {
            LoadingBox()
        } else if (userCourses.value!!.isEmpty()) {
            EmptyCollectionBox(
                modifier = modifier.padding(MaterialTheme.spacing.medium),
                bodyMessage = R.string.empty_course_collection_body
            )
        } else {
            HomeBody(
                modifier = modifier.padding(innerPadding),
                homeUiState = homeUiState.value,
                listState = scrollState,
                onCalendarBackClicked = {
                    homeViewModel.previousMonth()
                    coroutineScope.launch { scrollState.animateScrollToItem(1) }
                },
                onCalendarForwardClicked = {
                    homeViewModel.nextMonth()
                    coroutineScope.launch { scrollState.animateScrollToItem(1) }
                },
                doParty = homeViewModel::confetti,
                stopParty = homeViewModel::confettiStop
            )
        }
    }
}

@Composable
fun HomeBody(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    listState: LazyListState,
    onCalendarBackClicked: () -> Unit,
    onCalendarForwardClicked: () -> Unit,
    doParty: () -> Unit,
    stopParty: () -> Unit
) {
    if (homeUiState.isParty) {
        KonfettiView(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1F),
            parties = listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 15f,
                    damping = 0.9f,
                    angle = Angle.BOTTOM,
                    spread = Spread.ROUND,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(100),
                    position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                )
            ),
            updateListener = object : OnParticleSystemUpdateListener {
                override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                    if (activeSystems == 0) stopParty()
                }
            }
        )
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        contentPadding = PaddingValues(MaterialTheme.spacing.extraSmall),
        state = listState
    ) {
        if (homeUiState.networkError) {
            item {
                EmptyCollectionBox(
                    modifier = Modifier.padding(top = MaterialTheme.spacing.doubleExtraLarge),
                    bodyMessage = R.string.stats_network_error
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    StatisticContainer(
                        modifier = Modifier.weight(1F),
                        number = formatDurationAsText(homeUiState.courseStatistics.timeWatched),
                        label = stringResource(R.string.stats_hours_watched),
                        leadingContent = {
                            Icon(imageVector = Icons.Filled.Timer, contentDescription = null)
                        }
                    )
                    StatisticContainer(
                        modifier = Modifier.weight(1F),
                        number = formatDurationAsText(homeUiState.courseStatistics.timeWatchedToday),
                        label = stringResource(R.string.stats_hours_watched_today),
                        leadingContent = {
                            Icon(imageVector = Icons.Filled.Today, contentDescription = null)
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    StatisticContainer(
                        modifier = Modifier.weight(1F),
                        number = homeUiState.courseStatistics.videoCount.toString(),
                        label = stringResource(R.string.stats_videos_watched),
                        leadingContent = {
                            Icon(imageVector = Icons.Filled.SmartDisplay, contentDescription = null)
                        }
                    )
                    StatisticContainer(
                        modifier = Modifier.weight(1F),
                        number = stringResource(R.string.yay),
                        label = "",
                        leadingContent = {
                            Text(text = "🎉", style = MaterialTheme.typography.headlineMedium)
                        },
                        isClickable = true,
                        onClick = doParty
                    )
                }
            }
            item {
                MyInputLogCalendar(
                    yearMonth = homeUiState.selectedYearMonth,
                    dailyTotalTimes = homeUiState.monthlyAggregateData.map { durationInSeconds ->
                        (durationInSeconds / 60).toInt()
                    },
                    isLoading = homeUiState.isCalendarLoading,
                    onForwardClicked = onCalendarForwardClicked,
                    onBackClicked = onCalendarBackClicked,
                )
            }
        }
    }
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
        Box(
            modifier = Modifier
                .clickable(enabled = isClickable) { onClick() }
                .fillMaxSize()
        ) {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.default),
                leadingContent = leadingContent,
                headlineContent = {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                supportingContent = {
                    if (label.isNotBlank()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall
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
                }
            )
        }
    }
}