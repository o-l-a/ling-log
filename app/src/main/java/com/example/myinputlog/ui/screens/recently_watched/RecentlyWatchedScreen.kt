package com.example.myinputlog.ui.screens.recently_watched

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox

object RecentlyWatchedDestination : NavigationDestination {
    override val route: String = "recent"
    override val titleRes: Int = R.string.recently_watched_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyWatchedScreen(
    modifier: Modifier = Modifier,
    recentlyWatchedViewModel: RecentlyWatchedViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val recentlyWatchedUiState = recentlyWatchedViewModel.recentlyWatchedUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.RecentlyWatched,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(recentlyWatchedUiState.value.currentCourseId) }
            )
        }
    ) { innerPadding ->
        EmptyCollectionBox(
            modifier = modifier.padding(innerPadding),
            bodyMessage = com.google.firebase.installations.R.string.common_google_play_services_unknown_issue
        )
    }
}