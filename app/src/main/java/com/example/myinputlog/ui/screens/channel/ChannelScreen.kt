package com.example.myinputlog.ui.screens.channel

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.StatisticContainer
import com.example.myinputlog.ui.screens.utils.composable.channel.ChannelThumbnail
import com.example.myinputlog.ui.screens.utils.formatDurationAsText
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    modifier: Modifier = Modifier,
    channelViewModel: ChannelViewModel,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val channelUiState by channelViewModel.channelUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        MyInputLogTopAppBar(
            title = "",
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            hasDeleteAction = false,
            hasSaveAction = true,
            isFormValid = true,
            onSave = {},
            scrollBehavior = scrollBehavior
        )
    }) { innerPadding ->
        when (val currentState = channelUiState) {
            is ChannelUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            ChannelUiState.Loading -> {
                LoadingBox()
            }

            is ChannelUiState.Success -> {
                ChannelBody(Modifier.padding(innerPadding), currentState)
            }
        }
    }
}

@Composable
fun ChannelBody(
    modifier: Modifier = Modifier, channelUiState: ChannelUiState.Success
) {
    val scrollState = rememberLazyListState()
    val isScrollEnabled by remember {
        derivedStateOf {
            scrollState.canScrollForward || scrollState.canScrollBackward
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(MaterialTheme.spacing.extraSmall),
        state = scrollState,
        userScrollEnabled = isScrollEnabled
    ) {
        item {
            ChannelThumbnail(
                Modifier.height(MaterialTheme.spacing.doubleExtraLarge),
                channelUiState.channelMetadata.thumbnailHighUrl
            )
        }
        item {
            Text(
                modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall),
                text = channelUiState.channelMetadata.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
        channelUiState.channelMetadata.customUrl?.ifEmpty { null }?.let {
            item {
                Text(
                    text = channelUiState.channelMetadata.customUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                StatisticContainer(
                    modifier = Modifier.weight(1F),
                    number = channelUiState.channelMetadata.totalVideoCount.toString(),
                    label = stringResource(R.string.stats_videos_watched),
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.img_emoji_clapper),
                            contentDescription = "Clapper",
                            modifier = Modifier.size(MaterialTheme.spacing.statIconSize)
                        )
                    })
                StatisticContainer(
                    modifier = Modifier.weight(1F),
                    number = formatDurationAsText(channelUiState.channelMetadata.totalTimeInSeconds),
                    label = stringResource(R.string.stats_hours_watched),
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.img_emoji_clock),
                            contentDescription = "Clock",
                            modifier = Modifier.size(MaterialTheme.spacing.statIconSize)
                        )
                    })
            }
        }
    }
}