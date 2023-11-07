package com.example.myinputlog.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.MyInputLogDropdownField
import com.example.myinputlog.ui.theme.spacing

object HomeDestination : NavigationDestination {
    override val route: String = "home"
    override val titleRes: Int = R.string.home_screen_title
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: () -> Unit,
    navigateToYouTubeVideo: (String) -> Unit
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
        floatingActionButton = {
            if (userCourses.value != null && userCourses.value!!.isNotEmpty()) {
                FloatingActionButton(
                    modifier = Modifier.navigationBarsPadding(),
                    onClick = navigateToYouTubeVideoEntry
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                }
            }
        },
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Home,
                onBottomNavClicked = onBottomNavClicked
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
            )
        }
    }
}

@Composable
fun HomeBody(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    navigateToYouTubeVideo: (String) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("${homeUiState.hoursWatched.toFloat() + homeUiState.otherSourceHours.toFloat()}")
                Text("/")
                Text(homeUiState.goalInHours)
            }
        }
        item {
            CourseProgressIndicator(userCourse = homeUiState.toUserCourse())
        }
    }
}

@Composable
fun CourseProgressIndicator(
    modifier: Modifier = Modifier,
    userCourse: UserCourse
) {
    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.medium),
        progress = (userCourse.hoursWatched + userCourse.otherSourceHours) / userCourse.goalInHours
    )
}