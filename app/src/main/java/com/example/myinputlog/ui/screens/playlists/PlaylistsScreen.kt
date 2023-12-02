package com.example.myinputlog.ui.screens.playlists

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.remote.PlaylistItem
import com.example.myinputlog.data.remote.PlaylistSnippet
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.ChannelProfilePicture
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.ListItemPlaceholder
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.ext.hideEmail
import com.example.myinputlog.ui.screens.video_list.VideoContainer
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch

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
    navigateToYouTubeVideoEntryWithUrl: (String, String) -> Unit
) {
    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                playlistsViewModel.handleAuthorizationResponse(result.data!!)
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val playlistsUiState = playlistsViewModel.playlistsUiState.collectAsStateWithLifecycle()
    val videos = playlistsUiState.value.videos.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()

    val lazyColumnListState = rememberLazyListState()
    val currentIndex = remember { derivedStateOf { lazyColumnListState.firstVisibleItemIndex } }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (playlistsUiState.value.channelEmail.isNotBlank()) {
                YouTubeAccountTopAppBar(
                    channelPictureUrl = playlistsUiState.value.channelPictureUrl,
                    channelGivenName = playlistsUiState.value.channelGivenName,
                    channelFamilyName = playlistsUiState.value.channelFamilyName,
                    channelEmail = playlistsUiState.value.channelEmail,
                    onLogoutClicked = playlistsViewModel::signOutWithoutRedirect,
                    onAccountChangeClicked = { authorizationLauncher.launch(playlistsViewModel.attemptAuthorization()) },
                    onHideEmailClicked = playlistsViewModel::toggleHideEmail,
                    hideEmail = playlistsUiState.value.hideEmail,
                    scrollBehavior = scrollBehavior
                )
            }
        },
        bottomBar = {
            MyInputLogBottomNavBar(selectedScreen = Screen.RecentlyWatched,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(playlistsUiState.value.currentCourseId) })
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentIndex.value > 0,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            lazyColumnListState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        if (playlistsUiState.value.channelEmail.isBlank() && !playlistsUiState.value.isLoading) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    isFullSize = false,
                    bodyMessage = R.string.grant_access_yt_account_message
                )
                Button(
                    onClick = { authorizationLauncher.launch(playlistsViewModel.attemptAuthorization()) }
                ) {
                    Text(stringResource(R.string.choose_account_label))
                }
            }
        } else if (playlistsUiState.value.isLoading) {
            PlaylistsLoadingBody()
        } else if (videos.loadState.refresh is LoadState.Error || playlistsUiState.value.networkError) {
            EmptyCollectionBox(
                bodyMessage = R.string.network_error_playlists
            )
        } else {
            PlaylistsBody(
                modifier = Modifier.padding(innerPadding),
                playlistsUiState = playlistsUiState.value,
                videos = videos,
                lazyColumnListState = lazyColumnListState,
                onPlaylistClicked = playlistsViewModel::changePlaylist,
                navigateToYouTubeVideoEntryWithUrl = { videoUrl ->
                    navigateToYouTubeVideoEntryWithUrl(
                        playlistsUiState.value.currentCourseId,
                        videoUrl
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistsBody(
    modifier: Modifier = Modifier,
    playlistsUiState: PlaylistsUiState,
    videos: LazyPagingItems<YouTubeVideo>,
    lazyColumnListState: LazyListState,
    onPlaylistClicked: (String) -> Unit,
    navigateToYouTubeVideoEntryWithUrl: (String) -> Unit
) {
    val lazyRowListState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = lazyRowListState)
    LazyColumn(
        modifier = modifier,
        state = lazyColumnListState
    ) {
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(MaterialTheme.spacing.medium),
                state = lazyRowListState,
                flingBehavior = snapBehavior
            ) {
                playlistsUiState.playlistsData?.let {
                    item {
                        val id = PlaylistsViewModel.LIKED_PLAYLIST_ID
                        PlaylistCard(
                            playlistItem = PlaylistItem(
                                id = id,
                                snippet = PlaylistSnippet(stringResource(R.string.likes_playlist_title))
                            ),
                            selected = playlistsUiState.currentPlaylistId == id,
                            onPlaylistClicked = onPlaylistClicked
                        )
                    }
                }
                playlistsUiState.playlistsData?.let {
                    items(it.items) { playlistItem ->
                        PlaylistCard(
                            playlistItem = playlistItem,
                            selected = playlistsUiState.currentPlaylistId == playlistItem.id,
                            onPlaylistClicked = onPlaylistClicked
                        )
                    }
                }
            }
        }
        if (videos.itemCount > 0) {
            items(
                count = videos.itemCount, key = videos.itemKey()
            ) { index ->
                videos[index]?.let { video ->
                    VideoContainer(
                        video = video,
                        onVideoClicked = navigateToYouTubeVideoEntryWithUrl,
                        isPlaylistItem = true,
                        isClickable = playlistsUiState.currentCourseId.isNotBlank()
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
            items(10) {
                ListItemPlaceholder()
            }
        } else {
            item {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.empty_playlist
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    playlistItem: PlaylistItem,
    selected: Boolean = false,
    onPlaylistClicked: (String) -> Unit
) {
    FilterChip(
        modifier = modifier.padding(end = MaterialTheme.spacing.small),
        selected = selected,
        onClick = { onPlaylistClicked(playlistItem.id) },
        label = {
            Text(text = playlistItem.snippet.title)
        },
        trailingIcon = {
            if (selected) {
                Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = null)
            } else {
                Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeAccountTopAppBar(
    modifier: Modifier = Modifier,
    channelPictureUrl: String,
    channelGivenName: String,
    channelFamilyName: String,
    channelEmail: String,
    onLogoutClicked: () -> Unit,
    onAccountChangeClicked: () -> Unit,
    onHideEmailClicked: (Boolean) -> Unit,
    hideEmail: Boolean,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(modifier = modifier, scrollBehavior = scrollBehavior, navigationIcon = {
        ChannelProfilePicture(
            modifier = Modifier
                .padding(start = MaterialTheme.spacing.medium)
                .size(MaterialTheme.spacing.large), pictureUrl = channelPictureUrl
        )
    }, title = {
        ListItem(modifier = Modifier.fillMaxWidth(), headlineContent = {
            Text(
                text = "$channelGivenName $channelFamilyName",
                style = MaterialTheme.typography.bodyLarge
            )
        }, supportingContent = {
            Text(
                text = if (hideEmail) channelEmail.hideEmail() else channelEmail,
                style = MaterialTheme.typography.bodyMedium
            )
        })
    }, actions = {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopStart)
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.switch_yt_account)) },
                    onClick = {
                        onAccountChangeClicked()
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.logout_yt_account)) },
                    onClick = {
                        onLogoutClicked()
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(
                                if (hideEmail) {
                                    R.string.show_email
                                } else {
                                    R.string.hide_email
                                }
                            )
                        )
                    },
                    onClick = {
                        onHideEmailClicked(!hideEmail)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    })
}

@Composable
fun PlaylistsLoadingBody(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(10) {
            ListItemPlaceholder()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun YouTubeAccountTopAppBarPreview() {
    YouTubeAccountTopAppBar(
        channelPictureUrl = "",
        channelGivenName = "Toni",
        channelFamilyName = "Peperoni",
        channelEmail = "toni@peperoni.com",
        onLogoutClicked = { },
        onAccountChangeClicked = { },
        onHideEmailClicked = { },
        hideEmail = false
    )
}