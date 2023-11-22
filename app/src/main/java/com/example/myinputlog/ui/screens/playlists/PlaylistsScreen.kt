package com.example.myinputlog.ui.screens.playlists

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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.remote.PlaylistItem
import com.example.myinputlog.data.remote.PlaylistSnippet
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.ChannelProfilePicture
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.video_list.VideoContainer
import com.example.myinputlog.ui.theme.spacing

object PlaylistsDestination : NavigationDestination {
    override val route: String = "recent"
    override val titleRes: Int = R.string.recently_watched_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    modifier: Modifier = Modifier,
    playlistsViewModel: PlaylistsViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val playlistsUiState = playlistsViewModel.playlistsUiState.collectAsStateWithLifecycle()

    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                playlistsViewModel.handleAuthorizationResponse(result.data!!)
            }
        }
    }

    val videos = playlistsUiState.value.videos.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.RecentlyWatched,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(playlistsUiState.value.currentCourseId) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text(playlistsUiState.value.channelFamilyName)
            }
            item {
                ChannelProfilePicture(
                    pictureUrl = playlistsUiState.value.channelPictureUrl,
                    modifier = Modifier.size(MaterialTheme.spacing.extraLarge)
                )
            }
            item {
                Button(onClick = { authorizationLauncher.launch(playlistsViewModel.attemptAuthorization()) }) {
                    Text(text = "Log in")
                }
            }
            item {
                Button(onClick = { playlistsViewModel.signOutWithoutRedirect() }) {
                    Text(text = "Log out")
                }
            }
            item {
                Button(onClick = { playlistsViewModel.loadVideos() }) {
                    Text(text = "Do a flip")
                }
            }
            item {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    playlistsUiState.value.channelData?.let {
                        item {
                            val id = it.items[0].contentDetails.relatedPlaylists.likes
                            PlaylistCard(
                                playlistItem = PlaylistItem(
                                    id = id,
                                    snippet = PlaylistSnippet(stringResource(R.string.likes_playlist_title))
                                ),
                                selected = playlistsUiState.value.currentPlaylistId == id
                            )
                        }
                    }
                    playlistsUiState.value.playlistsData?.let {
                        items(it.items) { playlistItem ->
                            PlaylistCard(
                                playlistItem = playlistItem,
                                selected = playlistsUiState.value.currentPlaylistId == playlistItem.id
                            )
                        }
                    }
                }
            }
            item {
                Text(videos.itemCount.toString())
            }
            if (videos.itemCount > 0) {
                items(
                    count = videos.itemCount,
                    key = videos.itemKey()
                ) { index ->
                    videos[index]?.let { video ->
                        Text(index.toString())
                        VideoContainer(
                            video = video,
                            onVideoClicked = {
                            }
                        )
                    }
                }
                when (videos.loadState.append) {
                    is LoadState.NotLoading -> Unit
                    is LoadState.Loading -> {
                        item {
                            LoadingBox()
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            Text("Some error occurred")
                        }
                    }
                }
            } else if (videos.loadState.refresh is LoadState.Loading) {
                Unit
            } else {
                item {
                    EmptyCollectionBox(
                        modifier = modifier.padding(MaterialTheme.spacing.medium),
                        bodyMessage = R.string.empty_video_collection_body
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    playlistItem: PlaylistItem,
    selected: Boolean = false
) {
    Card(modifier = modifier.padding(MaterialTheme.spacing.small)) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.small)) {
            Text(
                text = playlistItem.snippet.title
            )
            if (selected) {
                Text("TO JA!!!")
            }
        }
    }
}