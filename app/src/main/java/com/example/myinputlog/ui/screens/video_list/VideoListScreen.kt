package com.example.myinputlog.ui.screens.video_list

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen

object VideoListDestination : NavigationDestination {
    override val route: String = "videos"
    override val titleRes: Int = R.string.video_list_screen_title
}

@Composable
fun VideoListScreen(
    modifier: Modifier = Modifier,
    videoListViewModel: VideoListViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: () -> Unit,
    navigateToYouTubeVideo: (String) -> Unit
) {
    val videoListUiState = videoListViewModel.videoListUiState.collectAsStateWithLifecycle()
    val userCourses = videoListUiState.value.userCourses.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = navigateToYouTubeVideoEntry
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            }
        },
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Videos,
                onBottomNavClicked = onBottomNavClicked
            )
        }
    ) { innerPadding ->
        Text(
            text = "VIDEOS",
            modifier = modifier.padding(innerPadding)
        )
    }
}