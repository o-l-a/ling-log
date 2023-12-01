package com.example.myinputlog.ui.screens.video_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.CourseTopAppBar
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.ListItemPlaceholder
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.VideoThumbnail
import com.example.myinputlog.ui.screens.utils.ext.formatAsListHeader
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch

object VideoListDestination : NavigationDestination {
    override val route: String = "videos"
    override val titleRes: Int = R.string.video_list_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    modifier: Modifier = Modifier,
    videoListViewModel: VideoListViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
    navigateToYouTubeVideo: (String, String) -> Unit
) {
    val videoListUiState = videoListViewModel.videoListUiState.collectAsStateWithLifecycle()
    val userCourses = videoListUiState.value.userCourses.collectAsStateWithLifecycle(emptyList())
    val videos = videoListUiState.value.videos.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()

    val lazyColumnListState = rememberLazyListState()
    val currentIndex = remember { derivedStateOf { lazyColumnListState.firstVisibleItemIndex } }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (userCourses.value != null && userCourses.value!!.isNotEmpty()) {
                CourseTopAppBar(
                    course = videoListUiState.value.toUserCourse(),
                    courseStatistics = videoListUiState.value.courseStatistics,
                    onValueChange = videoListViewModel::changeCurrentCourseId,
                    options = userCourses.value!!,
                    scrollBehavior = scrollBehavior
                )
            }
        },
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Videos,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(videoListUiState.value.currentCourseId) }
            )
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
        if (userCourses.value == null) {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
                contentPadding = PaddingValues(MaterialTheme.spacing.extraExtraSmall),
            ) {
                items(10) {
                    ListItemPlaceholder()
                }
            }
        } else if (userCourses.value!!.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.empty_course_collection_body
                )
            }
        } else {
            VideoListBody(
                modifier = modifier.padding(innerPadding),
                videoListUiState = videoListUiState.value,
                navigateToYouTubeVideo = navigateToYouTubeVideo,
                lazyColumnListState = lazyColumnListState,
                videos = videos
            )
        }
    }
}

@Composable
fun VideoListBody(
    modifier: Modifier = Modifier,
    videoListUiState: VideoListUiState,
    videos: LazyPagingItems<YouTubeVideo>,
    navigateToYouTubeVideo: (String, String) -> Unit,
    lazyColumnListState: LazyListState
) {
    if (videos.loadState.refresh is LoadState.Loading) {
        LoadingBox()
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
        contentPadding = PaddingValues(MaterialTheme.spacing.extraExtraSmall),
        state = lazyColumnListState
    ) {
        if (videos.itemCount > 0) {
            items(
                count = videos.itemCount,
                key = videos.itemKey()
            ) { index ->
                videos[index]?.let { video ->
                    VideoContainer(
                        video = video,
                        isSeparator = video.id.isBlank(),
                        onVideoClicked = {
                            navigateToYouTubeVideo(videoListUiState.currentCourseId, video.id)
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
            items(10) {
                ListItemPlaceholder()
            }
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

/**
 * Shows as a ListItem for a video and as a separator for just a date
 */
@Composable
fun VideoContainer(
    modifier: Modifier = Modifier,
    video: YouTubeVideo,
    isSeparator: Boolean = false,
    onVideoClicked: (String) -> Unit,
    isPlaylistItem: Boolean = false
) {
    if (!isSeparator) {
        ListItem(
            modifier = modifier.clickable {
                if (!isPlaylistItem) {
                    onVideoClicked(video.id)
                }
            },
            headlineContent = {
                Text(
                    text = video.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            supportingContent = {
                Text(
                    text = "${video.channel}${if (video.speakersNationality != null) " • " + video.speakersNationality.flagEmoji else ""}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            leadingContent = {
                VideoThumbnail(
                    modifier = Modifier.width(MaterialTheme.spacing.doubleExtraLarge),
                    videoUrl = video.thumbnailMediumUrl,
                    duration = video.durationInSeconds,
                    isListItemLeading = true
                )
            },
            trailingContent = {
                if (isPlaylistItem) {
                    IconButton(onClick = { onVideoClicked(video.videoUrl) }) {
                        Icon(
                            modifier = Modifier.padding(MaterialTheme.spacing.extraSmall),
                            imageVector = Icons.Filled.LibraryAdd,
                            contentDescription = null
                        )
                    }
                }
            }
        )
    } else {
        Text(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            text = video.watchedOn.formatAsListHeader(),
            style = MaterialTheme.typography.titleMedium
        )
    }
}