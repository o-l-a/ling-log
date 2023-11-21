package com.example.myinputlog.ui.screens.recently_watched

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.PlaylistItem
import com.example.myinputlog.data.model.PlaylistSnippet
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.ChannelProfilePicture
import com.example.myinputlog.ui.theme.spacing

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
    val recentlyWatchedUiState =
        recentlyWatchedViewModel.recentlyWatchedUiState.collectAsStateWithLifecycle()

    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                recentlyWatchedViewModel.handleAuthorizationResponse(result.data!!)
            }
        }
    }

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
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                ChannelProfilePicture(
                    pictureUrl = recentlyWatchedUiState.value.channelPictureUrl,
                    modifier = Modifier.size(MaterialTheme.spacing.extraLarge)
                )
            }
            item {
                Button(onClick = { authorizationLauncher.launch(recentlyWatchedViewModel.attemptAuthorization()) }) {
                    Text(text = "Log in")
                }
            }
            item {
                Button(onClick = { recentlyWatchedViewModel.signOutWithoutRedirect() }) {
                    Text(text = "Log out")
                }
            }
            item {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    recentlyWatchedUiState.value.channelData?.let {
                        item {
                            PlaylistCard(
                                playlistItem = PlaylistItem(
                                    id = it.items[0].contentDetails.relatedPlaylists.likes,
                                    snippet = PlaylistSnippet(stringResource(R.string.likes_playlist_title))
                                )
                            )
                        }
                    }
                    recentlyWatchedUiState.value.playlistsData?.let {
                        items(it.items) { playlistItem ->
                            PlaylistCard(playlistItem = playlistItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    playlistItem: PlaylistItem
) {
    Card(modifier = modifier.padding(MaterialTheme.spacing.small)) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.small)) {
            Text(
                text = playlistItem.snippet.title
            )
        }
    }
}