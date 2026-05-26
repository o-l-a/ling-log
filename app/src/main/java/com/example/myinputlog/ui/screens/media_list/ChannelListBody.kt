package com.example.myinputlog.ui.screens.media_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.R
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.channel.ChannelListItemPlaceholder
import com.example.myinputlog.ui.screens.utils.composable.channel.ChannelThumbnail
import com.example.myinputlog.ui.screens.utils.formatDurationAsText
import com.example.myinputlog.ui.theme.spacing

@Composable
fun ChannelListBody(
    modifier: Modifier = Modifier,
    channels: LazyPagingItems<YouTubeChannel>,
    navigateToYouTubeChannel: (String, String) -> Unit,
    currentCourseId: String,
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
        if (channels.itemCount > 0) {
            items(
                count = channels.itemCount, key = channels.itemKey()
            ) { index ->
                channels[index]?.let { channel ->
                    ChannelContainer(
                        channel = channel,
                        onChannelClicked = { navigateToYouTubeChannel(currentCourseId, channel.id) }
                    )
                }
            }
            when (channels.loadState.append) {
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
        } else if (channels.loadState.refresh is LoadState.Loading) {
            items(10) {
                ChannelListItemPlaceholder()
            }
        } else {
            item {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.empty_channel_collection_body
                )
            }
        }
    }
}


@Composable
fun ChannelContainer(
    modifier: Modifier = Modifier, channel: YouTubeChannel, onChannelClicked: (String) -> Unit = {}
) {
    ListItem(modifier = modifier.clickable { onChannelClicked(channel.id) }, headlineContent = {
        Text(
            text = channel.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }, supportingContent = {
        Text(
            text = "${stringResource(R.string.channel_video_count)}: ${channel.totalVideoCount} (${
                formatDurationAsText(
                    channel.totalTimeInSeconds
                )
            })",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )
    }, leadingContent = {
        ChannelThumbnail(
            modifier = Modifier.height(MaterialTheme.spacing.extraLarge),
            channelThumbnailUrl = channel.thumbnailMediumUrl
        )
    })
}