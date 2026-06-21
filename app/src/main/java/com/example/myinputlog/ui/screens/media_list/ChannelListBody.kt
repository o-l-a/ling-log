package com.example.myinputlog.ui.screens.media_list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.IntOffset
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.screens.common.composable.channel.ChannelListItemPlaceholder
import com.example.myinputlog.ui.screens.common.composable.channel.ChannelThumbnail
import com.example.myinputlog.ui.screens.common.composable.label.SmallLabelChipRow
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.theme.spacing

@Composable
fun ChannelListBody(
    modifier: Modifier = Modifier,
    channels: LazyPagingItems<ChannelUiModel>,
    navigateToYouTubeChannel: (String, String) -> Unit,
    currentCourseId: String,
    lazyColumnListState: LazyListState
) {
    val isInitialLoading =
        channels.loadState.refresh is LoadState.Loading && channels.itemCount == 0
    val isEmpty = channels.loadState.refresh is LoadState.NotLoading && channels.itemCount == 0

    val displayState = when {
        isInitialLoading -> ListDisplayState.Loading
        isEmpty -> ListDisplayState.Empty
        else -> ListDisplayState.Success
    }

    AnimatedContent(
        targetState = displayState, transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
        }, label = "ListStateTransition"
    ) { state ->
        when (state) {
            ListDisplayState.Loading -> {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.small + MaterialTheme.spacing.extraSmall,
                        bottom = MaterialTheme.spacing.medium,
                        start = MaterialTheme.spacing.extraExtraSmall,
                        end = MaterialTheme.spacing.extraExtraSmall
                    ),
                    state = lazyColumnListState
                ) {
                    items(10) {
                        ChannelListItemPlaceholder()
                    }
                }
            }

            ListDisplayState.Empty -> {
                EmptyCollectionBox(
                    modifier = modifier
                        .padding(MaterialTheme.spacing.medium)
                        .fillMaxSize(),
                    bodyMessage = R.string.empty_channel_collection_body
                )
            }

            ListDisplayState.Success -> {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.small + MaterialTheme.spacing.extraSmall,
                        bottom = MaterialTheme.spacing.medium,
                        start = MaterialTheme.spacing.extraExtraSmall,
                        end = MaterialTheme.spacing.extraExtraSmall
                    ),
                    state = lazyColumnListState
                ) {
                    if (channels.itemCount > 0) {
                        items(
                            count = channels.itemCount, key = channels.itemKey { it.id }) { index ->
                            channels[index]?.let { channel ->
                                ChannelContainer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(
                                            fadeInSpec = tween(300),
                                            fadeOutSpec = tween(300),
                                            placementSpec = spring(
                                                stiffness = Spring.StiffnessMediumLow,
                                                visibilityThreshold = IntOffset.VisibilityThreshold
                                            )
                                        ), channel = channel, onChannelClicked = {
                                        navigateToYouTubeChannel(
                                            currentCourseId, channel.id
                                        )
                                    })
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
                    }
                }
            }

        }
    }
}


@Composable
fun ChannelContainer(
    modifier: Modifier = Modifier, channel: ChannelUiModel, onChannelClicked: (String) -> Unit = {}
) {
    ListItem(modifier = modifier.clickable { onChannelClicked(channel.id) }, headlineContent = {
        Column {
            Text(
                text = channel.title,
                maxLines = channel.titleLines(),
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraExtraSmall))
            Text(
                text = channel.supportingLine(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
            SmallLabelChipRow(labels = channel.defaultLabels.toList())
        }
    }, leadingContent = {
        ChannelThumbnail(
            modifier = Modifier.height(MaterialTheme.spacing.extraLarge),
            channelThumbnailUrl = channel.thumbnailMediumUrl,
            rank = channel.rank
        )
    })
}