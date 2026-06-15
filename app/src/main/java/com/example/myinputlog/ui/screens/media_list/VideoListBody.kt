package com.example.myinputlog.ui.screens.media_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.screens.common.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.LoadingBox
import com.example.myinputlog.ui.screens.common.composable.label.SmallLabelChipRow
import com.example.myinputlog.ui.screens.common.composable.video.VideoListItemPlaceholder
import com.example.myinputlog.ui.screens.common.composable.video.VideoThumbnail
import com.example.myinputlog.ui.screens.common.ext.formatAsListHeader
import com.example.myinputlog.ui.theme.spacing

@Composable
fun VideoListBody(
    modifier: Modifier = Modifier,
    currentCourseId: String,
    videos: LazyPagingItems<VideoUiModel>,
    navigateToYouTubeVideo: (String, String) -> Unit,
    lazyColumnListState: LazyListState
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
        contentPadding = PaddingValues(
            top = MaterialTheme.spacing.small + MaterialTheme.spacing.extraSmall,
            bottom = MaterialTheme.spacing.extraExtraSmall,
            start = MaterialTheme.spacing.extraExtraSmall,
            end = MaterialTheme.spacing.extraExtraSmall
        ),
        state = lazyColumnListState
    ) {
        if (videos.itemCount > 0) {
            items(
                count = videos.itemCount, key = videos.itemKey()
            ) { index ->
                videos[index]?.let { video ->
                    VideoContainer(
                        video = video, isSeparator = video.id.isBlank(), onVideoClicked = {
                            navigateToYouTubeVideo(currentCourseId, video.id)
                        })
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
                VideoListItemPlaceholder()
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
    video: VideoUiModel,
    isSeparator: Boolean = false,
    onVideoClicked: (String) -> Unit
) {
    if (!isSeparator) {
        ListItem(modifier = modifier.clickable { onVideoClicked(video.id) }, headlineContent = {
            Column {
                Text(
                    text = video.title,
                    maxLines = video.titleLines(),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraExtraSmall))
                Text(
                    text = video.supportingLine(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                SmallLabelChipRow(labels = video.labels.toList())
            }
        }, leadingContent = {
            VideoThumbnail(
                modifier = Modifier.height(MaterialTheme.spacing.extraLarge + MaterialTheme.spacing.small),
                videoThumbnailUrl = video.thumbnailMediumUrl,
                duration = video.durationInSeconds,
                isListItemLeading = true
            )
        })
    } else {
        Text(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            text = video.watchedOn.formatAsListHeader(),
            style = MaterialTheme.typography.titleMedium
        )
    }
}