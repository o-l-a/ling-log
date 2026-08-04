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
import com.example.myinputlog.ui.models.VideoListItem
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.screens.common.composable.label.SmallLabelChipRow
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.screens.common.composable.video.VideoListItemPlaceholder
import com.example.myinputlog.ui.screens.common.composable.video.VideoThumbnail
import com.example.myinputlog.ui.theme.spacing

internal enum class ListDisplayState {
    Loading, Empty, Success
}

@Composable
fun VideoListBody(
    modifier: Modifier = Modifier,
    currentCourseId: String,
    videos: LazyPagingItems<VideoListItem>,
    navigateToYouTubeVideo: (String, String) -> Unit,
    lazyColumnListState: LazyListState
) {
    val isInitialLoading = videos.loadState.refresh is LoadState.Loading && videos.itemCount == 0
    val isEmpty = videos.loadState.refresh is LoadState.NotLoading && videos.itemCount == 0

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
                        top = MaterialTheme.spacing.smallPlus,
                        bottom = MaterialTheme.spacing.medium,
                        start = MaterialTheme.spacing.extraExtraSmall,
                        end = MaterialTheme.spacing.extraExtraSmall
                    )
                ) {
                    items(10) {
                        VideoListItemPlaceholder()
                    }
                }
            }

            ListDisplayState.Empty -> {
                EmptyCollectionBox(
                    modifier = modifier
                        .padding(MaterialTheme.spacing.medium)
                        .fillMaxSize(),
                    bodyMessage = R.string.empty_video_collection_body
                )
            }

            ListDisplayState.Success -> {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.smallPlus,
                        bottom = MaterialTheme.spacing.medium,
                        start = MaterialTheme.spacing.extraExtraSmall,
                        end = MaterialTheme.spacing.extraExtraSmall
                    ),
                    state = lazyColumnListState
                ) {
                    if (videos.itemCount > 0) {
                        items(
                            count = videos.itemCount, key = videos.itemKey { item ->
                                when (item) {
                                    is VideoListItem.Video -> "video_${item.video.id}"
                                    is VideoListItem.Separator -> "sep_${item.title.hashCode()}"
                                }
                            }) { index ->
                            videos[index]?.let {
                                val animatedModifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(
                                        fadeInSpec = tween(300),
                                        fadeOutSpec = tween(300),
                                        placementSpec = spring(
                                            stiffness = Spring.StiffnessMediumLow,
                                            visibilityThreshold = IntOffset.VisibilityThreshold
                                        )
                                    )

                                when (val item = videos[index]) {
                                    is VideoListItem.Video -> {
                                        VideoContainer(
                                            modifier = animatedModifier,
                                            video = item.video,
                                            onVideoClicked = {
                                                navigateToYouTubeVideo(
                                                    currentCourseId, item.video.id
                                                )
                                            })
                                    }

                                    is VideoListItem.Separator -> {
                                        SeparatorContainer(
                                            modifier = animatedModifier,
                                            separatorTitle = item.title.asString()
                                        )
                                    }

                                    else -> {}
                                }
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
                    }
                }
            }
        }
    }
}

/**
 * Shows as a ListItem for a video
 */
@Composable
fun VideoContainer(
    modifier: Modifier = Modifier, video: VideoUiModel, onVideoClicked: (String) -> Unit
) {
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
            modifier = Modifier.height(MaterialTheme.spacing.extraLargePlus),
            videoThumbnailUrl = video.thumbnailHighUrl,
            duration = video.durationInSeconds,
            isListItemLeading = true
        )
    })
}

/**
 * Shows as a separator for just a date
 */
@Composable
fun SeparatorContainer(
    modifier: Modifier = Modifier, separatorTitle: String
) {
    Text(
        modifier = modifier.padding(horizontal = MaterialTheme.spacing.medium),
        text = separatorTitle,
        style = MaterialTheme.typography.titleMedium
    )
}