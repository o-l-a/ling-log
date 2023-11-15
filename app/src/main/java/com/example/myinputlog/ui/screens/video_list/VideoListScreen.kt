package com.example.myinputlog.ui.screens.video_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.MyInputLogDropdownField
import com.example.myinputlog.ui.screens.utils.composable.VideoThumbnail
import com.example.myinputlog.ui.theme.spacing

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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (userCourses.value != null) {
                TopAppBar(
                    title = {
                        MyInputLogDropdownField(
                            value = videoListUiState.value.toUserCourse(),
                            onValueChange = videoListViewModel::changeCurrentCourseId,
                            options = userCourses.value!!
                        )
                    },
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
        }
    ) { innerPadding ->
        if (userCourses.value == null) {
            LoadingBox()
        } else if (userCourses.value!!.isEmpty()) {
            EmptyCollectionBox(
                modifier = modifier.padding(MaterialTheme.spacing.medium),
                bodyMessage = R.string.empty_course_collection_body
            )
        } else if (videos.itemCount == 0) {
            EmptyCollectionBox(
                modifier = modifier.padding(MaterialTheme.spacing.medium),
                bodyMessage = R.string.empty_video_collection_body
            )
        } else {
            VideoListBody(
                modifier = modifier.padding(innerPadding),
                videoListUiState = videoListUiState.value,
                navigateToYouTubeVideo = navigateToYouTubeVideo,
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
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
        contentPadding = PaddingValues(MaterialTheme.spacing.extraExtraSmall)
    ) {
        items(
            count = videos.itemCount,
            key = videos.itemKey()
        ) { index ->
            videos[index]?.let { video ->
                Text(modifier = Modifier.padding(0.dp), text = index.toString())
                VideoContainer(
                    video = video,
                    onVideoClicked = {
                        navigateToYouTubeVideo(videoListUiState.currentCourseId, video.id)
                    }
                )
            }
        }
        when (videos.loadState.append) {
            is LoadState.NotLoading -> Unit
            LoadState.Loading -> {
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
    }
}

@Composable
fun VideoContainer(
    modifier: Modifier = Modifier,
    video: YouTubeVideo,
    onVideoClicked: (String) -> Unit
) {
    ListItem(
        modifier = modifier.clickable { onVideoClicked(video.id) },
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
    )
}
