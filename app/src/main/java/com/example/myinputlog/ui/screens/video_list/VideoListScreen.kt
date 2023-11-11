package com.example.myinputlog.ui.screens.video_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val videos = videoListUiState.value.videos.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            if (userCourses.value != null) {
                MyInputLogDropdownField(
                    value = videoListUiState.value.toUserCourse(),
                    onValueChange = videoListViewModel::changeCurrentCourseId,
                    options = userCourses.value!!
                )
            }
        },
        floatingActionButton = {
            if (userCourses.value != null && userCourses.value!!.isNotEmpty()) {
                FloatingActionButton(
                    modifier = Modifier.navigationBarsPadding(),
                    onClick = { navigateToYouTubeVideoEntry(videoListUiState.value.currentCourseId) }
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
                selectedScreen = Screen.Videos,
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
        } else if (videos.value == null || videos.value!!.isEmpty()) {
            EmptyCollectionBox(
                modifier = modifier.padding(MaterialTheme.spacing.medium),
                bodyMessage = R.string.empty_video_collection_body
            )
        } else {
            VideoListBody(
                modifier = modifier.padding(innerPadding),
                videoListUiState = videoListUiState.value,
                videos = videos.value!!,
                navigateToYouTubeVideo = navigateToYouTubeVideo
            )
        }
    }
}

@Composable
fun VideoListBody(
    modifier: Modifier = Modifier,
    videoListUiState: VideoListUiState,
    videos: List<YouTubeVideo>,
    navigateToYouTubeVideo: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
        contentPadding = PaddingValues(MaterialTheme.spacing.extraExtraSmall)
    ) {
        items(videos) { video ->
            VideoContainer(
                video = video,
                onVideoClicked = {
                    navigateToYouTubeVideo(videoListUiState.currentCourseId, video.id)
                }
            )
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
