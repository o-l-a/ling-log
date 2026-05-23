package com.example.myinputlog.ui.screens.media_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.R
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.ListItemPlaceholder
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.theme.spacing

@Composable
fun ChannelListBody(
    modifier: Modifier = Modifier,
    channels: LazyPagingItems<YouTubeChannel>,
    lazyColumnListState: LazyListState
) {
    if (channels.loadState.refresh is LoadState.Loading) {
        LoadingBox()
    }
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
                    Text(channel.title)
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
                ListItemPlaceholder()
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