package com.example.myinputlog.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.MyInputLogDropdownField
import com.example.myinputlog.ui.screens.utils.formatDurationAsText
import com.example.myinputlog.ui.theme.spacing
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

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
    navigateToYouTubeVideo: (String, String) -> Unit
) {
    val homeUiState = homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val userCourses = homeUiState.value.userCourses.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            if (userCourses.value != null) {
                MyInputLogDropdownField(
                    value = homeUiState.value.toUserCourse(),
                    onValueChange = homeViewModel::changeCurrentCourseId,
                    options = userCourses.value!!
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
                navigateToYouTubeVideo = navigateToYouTubeVideo,
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
    navigateToYouTubeVideo: (String, String) -> Unit,
    doParty: () -> Unit,
    stopParty: () -> Unit
) {
    val hoursWatched = homeUiState.courseStatistics.timeWatched
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
        contentPadding = PaddingValues(MaterialTheme.spacing.extraSmall)
    ) {
        if (homeUiState.networkError) {
            item {
                EmptyCollectionBox(bodyMessage = R.string.stats_network_error)
            }
        } else {
            item {
                CourseProgressIndicator(
                    hoursWatched = hoursWatched,
                    otherSourceHours = homeUiState.otherSourceHours.toLong() * 3600,
                    goalInHours = homeUiState.goalInHours.toLong()
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    StatisticContainer(
                        modifier = Modifier.weight(1F),
                        number = formatDurationAsText(homeUiState.courseStatistics.timeWatched),
                        label = stringResource(R.string.stats_hours_watched)
                    )
                    StatisticContainer(
                        modifier = Modifier.weight(1F),
                        number = formatDurationAsText(homeUiState.courseStatistics.timeWatchedToday),
                        label = stringResource(R.string.stats_hours_watched_today)
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
                        label = stringResource(R.string.stats_videos_watched)
                    )
                    StatisticContainer(
                        modifier = Modifier.weight(1F),
                        number = "🎉",
                        label = stringResource(R.string.yay),
                        isClickable = true,
                        onClick = doParty
                    )
                }
            }
        }
    }
}

@Composable
fun CourseProgressIndicator(
    modifier: Modifier = Modifier,
    hoursWatched: Long,
    otherSourceHours: Long,
    goalInHours: Long
) {
    val progress = (hoursWatched + otherSourceHours).toFloat() / (goalInHours * 3600)
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(MaterialTheme.spacing.large)
            .padding(horizontal = MaterialTheme.spacing.small)
            .padding(bottom = MaterialTheme.spacing.extraSmall),
        shape = RoundedCornerShape(MaterialTheme.spacing.medium)
    ) {
        Row {
            Column(
                modifier = Modifier
                    .weight(if (progress > 0) progress else 0.1F)
                    .padding(MaterialTheme.spacing.default)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.medium))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.padding(start = MaterialTheme.spacing.medium),
                    text = formatDurationAsText(hoursWatched + otherSourceHours),
                    textAlign = TextAlign.Left
                )
            }
            Column(
                modifier = Modifier
                    .weight(if (progress > 0) 1 - progress else 0.99F)
                    .padding(MaterialTheme.spacing.default)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.padding(end = MaterialTheme.spacing.medium),
                    text = "${goalInHours}h",
                    textAlign = TextAlign.Right
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
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.small)
    ) {
        Box(
            modifier = Modifier
                .clickable(enabled = isClickable) { onClick() }
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.small),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.small),
                    text = number,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
fun StatisticContainerPreview() {
    StatisticContainer(
        modifier = Modifier.width(MaterialTheme.spacing.doubleExtraLarge),
        number = "10",
        label = "label"
    )
}

@Preview
@Composable
fun ProgressIndicatorPreview() {
    CourseProgressIndicator(
        modifier = Modifier.width(MaterialTheme.spacing.doubleExtraLarge),
        hoursWatched = 10,
        otherSourceHours = 10,
        goalInHours = 50
    )
}