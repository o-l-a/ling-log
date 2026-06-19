package com.example.myinputlog.ui.screens.media_list

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.FilterContentType
import com.example.myinputlog.ui.models.FilterValueUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.common.composable.bars.MediaListTopAppBar
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogBottomNavBar
import com.example.myinputlog.ui.screens.common.composable.input.FilterItemRow
import com.example.myinputlog.ui.screens.common.composable.input.filterArea
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.video.VideoListItemPlaceholder
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch

sealed class MediaTab(
    @get:StringRes val resourceId: Int
) {
    object Videos : MediaTab(R.string.video_list_screen_title)
    object Channels : MediaTab(R.string.channel_list_screen_title)
}

private val tabs = listOf(MediaTab.Videos, MediaTab.Channels)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListScreen(
    modifier: Modifier = Modifier,
    mediaListViewModel: MediaListViewModel,
    onBottomNavClicked: (Any) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
    navigateToYouTubeVideo: (String, String) -> Unit,
    navigateToYouTubeChannel: (String, String) -> Unit
) {
    val mediaListUiState by mediaListViewModel.mediaListUiState.collectAsStateWithLifecycle()
    val currentCourseId by mediaListViewModel.currentCourseId.collectAsStateWithLifecycle()

    val videos = mediaListViewModel.videoFlow.collectAsLazyPagingItems()
    val channels = mediaListViewModel.channelFlow.collectAsLazyPagingItems()
    val filterChannels = mediaListViewModel.filterChannelFlow.collectAsLazyPagingItems()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val videoLazyListState = rememberLazyListState()
    val channelLazyListState = rememberLazyListState()
    val activeListState by remember {
        derivedStateOf {
            if (pagerState.currentPage == 0) videoLazyListState else channelLazyListState
        }
    }

    var showFilterSheet by remember { mutableStateOf(false) }

    val showFab by remember {
        derivedStateOf { activeListState.firstVisibleItemIndex > 0 }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (mediaListUiState is MediaListUiState.Success) {
                MediaListHeader(
                    onSearch = mediaListViewModel::updateSearchQuery,
                    onFilterClick = { showFilterSheet = true },
                    scrollBehavior = scrollBehavior,
                    pagerState = pagerState,
                    tabs = tabs,
                    hasActiveFilters = (mediaListUiState as MediaListUiState.Success).filters.hasActiveFilters(
                        pagerState.currentPage == 1
                    )
                )
            }
        },
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Videos,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(currentCourseId) })
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            activeListState.animateScrollToItem(0)
                        }
                    }) {
                    Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null)
                }
            }
        }) { innerPadding ->
        when (val currentState = mediaListUiState) {
            is MediaListUiState.Loading -> {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraExtraSmall),
                    contentPadding = PaddingValues(MaterialTheme.spacing.extraExtraSmall),
                ) {
                    items(10) {
                        VideoListItemPlaceholder()
                    }
                }
            }

            is MediaListUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    EmptyCollectionBox(
                        modifier = modifier.padding(MaterialTheme.spacing.medium),
                        bodyMessage = R.string.empty_course_collection_body_other_tabs
                    )
                }
            }

            is MediaListUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            is MediaListUiState.NetworkError -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            is MediaListUiState.Success -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    beyondViewportPageCount = 1
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> VideoListBody(
                            currentCourseId = currentState.courseHeader.id,
                            navigateToYouTubeVideo = navigateToYouTubeVideo,
                            lazyColumnListState = videoLazyListState,
                            videos = videos
                        )

                        1 -> ChannelListBody(
                            currentCourseId = currentState.courseHeader.id,
                            navigateToYouTubeChannel = navigateToYouTubeChannel,
                            lazyColumnListState = channelLazyListState,
                            channels = channels
                        )
                    }
                }
                if (showFilterSheet) {
                    MediaFilterBottomSheet(
                        currentTabIndex = pagerState.currentPage,
                        filters = currentState.filters,
                        labels = currentState.allLabels,
                        channels = filterChannels,
                        onLabelsChanged = mediaListViewModel::updateSelectedLabels,
                        onChannelsChanged = mediaListViewModel::updateSelectedChannels,
                        onDismiss = { showFilterSheet = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListHeader(
    scrollBehavior: TopAppBarScrollBehavior,
    pagerState: PagerState,
    tabs: List<MediaTab>,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()

    val fraction = scrollBehavior.state.overlappedFraction
    val appBarColors = TopAppBarDefaults.topAppBarColors()
    val backgroundColor = lerp(
        start = appBarColors.containerColor,
        stop = appBarColors.scrolledContainerColor,
        fraction = fraction
    )

    val elevation = lerp(0.dp, 3.dp, fraction)
    val textFieldState = rememberTextFieldState()

    Surface(
        color = backgroundColor, tonalElevation = elevation
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MediaListTopAppBar(
                textFieldState = textFieldState,
                onSearch = onSearch,
                onFilterClick = onFilterClick,
                hasActiveFilters = hasActiveFilters,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
            )
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            selectedTabIndex = pagerState.currentPage, matchContentSize = false
                        ), width = MaterialTheme.spacing.extraLarge + MaterialTheme.spacing.large
                    )
                }) {
                tabs.forEachIndexed { index, tab ->
                    Tab(selected = pagerState.currentPage == index, onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    }, text = { Text(stringResource(tab.resourceId)) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFilterBottomSheet(
    currentTabIndex: Int,
    filters: MediaFilters,
    labels: Set<LabelUiModel>,
    channels: LazyPagingItems<ChannelUiModel>,
    onLabelsChanged: (Set<String>) -> Unit,
    onChannelsChanged: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        confirmValueChange = { true })

    val scrollState = rememberLazyListState()

    var isLabelsExpanded by remember { mutableStateOf(false) }
    var isChannelsExpanded by remember { mutableStateOf(false) }

    val labelTitle = stringResource(R.string.label_list_nav_description)
    val channelTitle = stringResource(R.string.channel_list_screen_title)

    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.small)
                .padding(bottom = MaterialTheme.spacing.large), contentPadding = PaddingValues(
                MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall,
            ), state = scrollState
        ) {
            item {
                Text(
                    text = stringResource(R.string.filters_text),
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
                )
            }

            filterArea(
                title = labelTitle,
                items = labels.toList(),
                isExpanded = isLabelsExpanded,
                onHeaderClick = { isLabelsExpanded = !isLabelsExpanded },
                key = { it.id }) { label ->
                FilterItemRow(
                    filter = FilterValueUiModel(
                        id = label.id, content = FilterContentType.Labeled(
                            text = label.title,
                            colorRes = Color(label.color),
                            textColorRes = Color(label.textColor)
                        ), selected = filters.selectedLabels.contains(label.id)
                    ), onCheckedChange = { id ->
                        val newSet = if (filters.selectedLabels.contains(id)) {
                            filters.selectedLabels - id
                        } else {
                            filters.selectedLabels + id
                        }
                        onLabelsChanged(newSet)
                    })
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }

            filterArea(
                title = channelTitle,
                pagingItems = channels,
                isExpanded = isChannelsExpanded,
                onHeaderClick = { isChannelsExpanded = !isChannelsExpanded },
                key = { it.id }) { channel ->
                FilterItemRow(
                    filter = FilterValueUiModel(
                        id = channel.id,
                        content = FilterContentType.Basic(text = channel.title),
                        selected = filters.selectedChannels.contains(channel.id)
                    ), onCheckedChange = { id ->
                        val newSet = if (filters.selectedChannels.contains(id)) {
                            filters.selectedChannels - id
                        } else {
                            filters.selectedChannels + id
                        }
                        onChannelsChanged(newSet)
                    })
            }

            if (filters.hasActiveFilters(currentTabIndex == 1)) {
                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TextButton(
                            onClick = {
                                onLabelsChanged(emptySet())
                                onChannelsChanged(emptySet())
                                onDismiss()
                            }) {
                            Text(stringResource(R.string.clear_filters_text))
                        }
                        TextButton(
                            onClick = {
                                onDismiss()
                            }) {
                            Text(stringResource(R.string.apply_filters_text))
                        }
                    }
                }
            }
        }
    }
}