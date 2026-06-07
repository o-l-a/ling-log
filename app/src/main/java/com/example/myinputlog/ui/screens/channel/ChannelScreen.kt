package com.example.myinputlog.ui.screens.channel

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.SpinningClockIcon
import com.example.myinputlog.ui.screens.utils.composable.StatisticContainer
import com.example.myinputlog.ui.screens.utils.composable.channel.ChannelThumbnail
import com.example.myinputlog.ui.screens.utils.composable.label.LabelPickerTextField
import com.example.myinputlog.ui.screens.utils.formatDurationAsText
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    modifier: Modifier = Modifier, channelViewModel: ChannelViewModel, onNavigateUp: () -> Unit,
) {
    val channelUiState by channelViewModel.channelUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        MyInputLogTopAppBar(
            title = stringResource(R.string.video_channel_label),
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
                ChannelBody(
                    Modifier.padding(innerPadding),
                    currentState,
                    onQueryChange = channelViewModel::onQueryChange,
                    onItemRemoved = channelViewModel::removeLabel,
                    onItemSelected = channelViewModel::addLabel
                )
            }
        }
    }
}

@Composable
fun ChannelBody(
    modifier: Modifier = Modifier,
    channelUiState: ChannelUiState.Success,
    onQueryChange: (String) -> Unit,
    onItemSelected: (LabelUiModel) -> Unit,
    onItemRemoved: (LabelUiModel) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val isScrollEnabled by remember {
        derivedStateOf {
            scrollState.canScrollForward || scrollState.canScrollBackward
        }
    }
    var clockSpinTrigger by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(MaterialTheme.spacing.extraSmall),
        state = scrollState,
        userScrollEnabled = isScrollEnabled
    ) {
        item {
            ChannelThumbnail(
                Modifier.height(MaterialTheme.spacing.doubleExtraLarge),
                channelUiState.channelUiModel.thumbnailHighUrl
            )
        }
        item {
            Text(
                modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall),
                text = channelUiState.channelUiModel.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
        channelUiState.channelUiModel.customUrl?.ifEmpty { null }?.let {
            item {
                Text(
                    text = channelUiState.channelUiModel.customUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = MaterialTheme.spacing.small, bottom = MaterialTheme.spacing.medium
                    )
                    .padding(horizontal = MaterialTheme.spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                StatisticContainer(
                    modifier = Modifier.weight(1F),
                    number = channelUiState.channelUiModel.totalVideoCount.toString(),
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
                    number = formatDurationAsText(channelUiState.channelUiModel.totalTimeInSeconds),
                    label = stringResource(R.string.stats_hours_watched),
                    leadingContent = {
                        SpinningClockIcon(
                            spinTrigger = clockSpinTrigger,
                            modifier = Modifier.size(MaterialTheme.spacing.statIconSize)
                        )
                    },
                    isClickable = true,
                    onClick = { clockSpinTrigger++ })
            }
        }
        item {
            LabelPickerTextField(
                modifier = Modifier.padding(
                    start = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.small
                ),
                label = stringResource(R.string.labels_text_field_label),
                placeholder = stringResource(R.string.labels_search_placeholder),
                searchQuery = channelUiState.searchQuery,
                selectedItems = channelUiState.channelUiModel.defaultLabels,
                suggestions = channelUiState.suggestions,
                onQueryChange = { onQueryChange(it) },
                onItemSelected = { onItemSelected(it) },
                onItemRemoved = { onItemRemoved(it) },
            )
        }
    }
}