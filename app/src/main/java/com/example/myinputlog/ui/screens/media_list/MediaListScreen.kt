package com.example.myinputlog.ui.screens.media_list

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.models.FilterContentType
import com.example.myinputlog.ui.models.FilterValueUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.common.composable.bars.MediaListTopAppBar
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogBottomNavBar
import com.example.myinputlog.ui.screens.common.composable.input.FilterChange
import com.example.myinputlog.ui.screens.common.composable.input.FilterItemRow
import com.example.myinputlog.ui.screens.common.composable.input.SortItemRow
import com.example.myinputlog.ui.screens.common.composable.input.filterArea
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.video.VideoListItemPlaceholder
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch

sealed class MediaTab(
    @get:StringRes val resourceId: Int, val count: Int
) {
    data class Videos(val videoCount: Int) : MediaTab(R.string.video_list_screen_title, videoCount)
    data class Channels(val channelCount: Int) :
        MediaTab(R.string.channel_list_screen_title, channelCount)
}

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
    val scrollToIndex by mediaListViewModel.scrollToIndex.collectAsStateWithLifecycle()
    val currentCourseId by mediaListViewModel.currentCourseId.collectAsStateWithLifecycle()

    val videos = mediaListViewModel.videoFlow.collectAsLazyPagingItems()
    val channels = mediaListViewModel.channelFlow.collectAsLazyPagingItems()
    val filterChannels = mediaListViewModel.filterChannelFlow.collectAsLazyPagingItems()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val coroutineScope = rememberCoroutineScope()

    val tabs = remember(mediaListUiState) {
        val state = mediaListUiState as? MediaListUiState.Success
        if (state != null) {
            listOf(
                MediaTab.Videos(state.videoCount), MediaTab.Channels(state.channelCount)
            )
        } else {
            listOf(
                MediaTab.Videos(0), MediaTab.Channels(0)
            )
        }
    }

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

    LaunchedEffect(scrollToIndex, videos.loadState.refresh) {
        val targetIndex = scrollToIndex
        if (targetIndex != null && videos.loadState.refresh is androidx.paging.LoadState.NotLoading) {
            videoLazyListState.scrollToItem(targetIndex)
            mediaListViewModel.onScrollConsumed()
        }
    }

    if (showFilterSheet && mediaListUiState is MediaListUiState.Success) {
        val successState = mediaListUiState as MediaListUiState.Success
        MediaFilterBottomSheet(
            currentTabIndex = pagerState.currentPage,
            filters = successState.filters,
            labels = successState.allLabels,
            countries = successState.allCountries,
            channels = filterChannels,
            onLabelsChanged = mediaListViewModel::updateSelectedLabels,
            onSelectAllLabels = mediaListViewModel::onSelectAllLabelsChange,
            onSelectUnassignedLabel = mediaListViewModel::onUnassignedLabelsChange,
            onCountriesChanged = mediaListViewModel::updateSelectedCountries,
            onSelectAllCountries = mediaListViewModel::onSelectAllCountriesChange,
            onChannelsChanged = mediaListViewModel::updateSelectedChannels,
            onSelectAllChannels = mediaListViewModel::onSelectAllChannelsChange,
            onSelectUnassignedCountry = mediaListViewModel::onUnassignedCountriesChange,
            onApplyClicked = mediaListViewModel::applyFilters,
            onClearClicked = mediaListViewModel::clearFilters,
            appliedSort = successState.appliedSort,
            onSortChanged = mediaListViewModel::onSortChange,
            onDismiss = { showFilterSheet = false })
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
                    tabs = tabs
                )
            }
        },
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Videos,
                onBottomNavClicked = onBottomNavClicked,
                navigateToVideoEntry = { navigateToYouTubeVideoEntry(currentCourseId) })
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
                            activeListState.scrollToItem(0)
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
                            currentCourseId = currentState.currentCourseId,
                            navigateToYouTubeVideo = navigateToYouTubeVideo,
                            lazyColumnListState = videoLazyListState,
                            appliedSort = currentState.appliedSort,
                            videos = videos
                        )

                        1 -> ChannelListBody(
                            currentCourseId = currentState.currentCourseId,
                            navigateToYouTubeChannel = navigateToYouTubeChannel,
                            lazyColumnListState = channelLazyListState,
                            channels = channels
                        )
                    }
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
    onFilterClick: () -> Unit
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
                        ), width = MaterialTheme.spacing.extraLargeTriplePlus
                    )
                }) {
                tabs.forEachIndexed { index, tab ->
                    Tab(selected = pagerState.currentPage == index, onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    }, text = { Text("${stringResource(tab.resourceId)} (${tab.count})") })
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFilterBottomSheet(
    currentTabIndex: Int,
    filters: MediaFilters,
    labels: Set<LabelUiModel>,
    channels: LazyPagingItems<ChannelUiModel>,
    countries: Set<CountryUiModel>,
    onLabelsChanged: (Set<String>) -> Unit,
    onSelectAllLabels: (FilterChange) -> Unit,
    onSelectUnassignedLabel: (FilterChange) -> Unit,
    onChannelsChanged: (Set<String>) -> Unit,
    onSelectAllChannels: (FilterChange) -> Unit,
    onCountriesChanged: (Set<String>) -> Unit,
    onSelectAllCountries: (FilterChange) -> Unit,
    onSelectUnassignedCountry: (FilterChange) -> Unit,
    onApplyClicked: () -> Unit,
    onClearClicked: () -> Unit,
    appliedSort: SortOptions,
    onSortChanged: (SortOptions) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        confirmValueChange = { true })

    val scrollState = rememberLazyListState()

    var isLabelsExpanded by remember { mutableStateOf(false) }
    var isChannelsExpanded by remember { mutableStateOf(false) }
    var isCountriesExpanded by remember { mutableStateOf(false) }
    var isSortExpanded by remember { mutableStateOf(false) }
    val isChannel by remember { derivedStateOf { currentTabIndex == 1 } }

    val labelTitle = stringResource(R.string.label_list_nav_description)
    val channelTitle = stringResource(R.string.channel_list_screen_title)
    val countryTitle = stringResource(R.string.countries_description)
    val sortTitle = stringResource(R.string.sort_header)
    val emptyText = stringResource(R.string.empty_option_text)

    val dismiss = {
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth(),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle(
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = MaterialTheme.spacing.small)
                .padding(bottom = MaterialTheme.spacing.large), state = scrollState
        ) {
            filterArea(
                title = sortTitle,
                items = SortOptions.entries,
                isExpanded = isSortExpanded,
                enableSelectAll = false,
                isAllSelected = false,
                onHeaderClick = { isSortExpanded = !isSortExpanded },
                onSelectAll = {},
                key = { it.name }) { option ->
                SortItemRow(
                    sort = option,
                    onClick = onSortChanged,
                    isSelected = option == appliedSort,
                    isEnabled = option in if (isChannel) SortOptions.channelSortOptions()
                        .toList() else SortOptions.videoSortOptions().toList()
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }

            filterArea(
                title = labelTitle,
                unassignedText = emptyText,
                items = labels.toList(),
                isExpanded = isLabelsExpanded,
                enableSelectAll = true,
                enableUnassigned = true,
                isAllSelected = filters.allLabelsSelected,
                isUnassignedSelected = filters.unassignedLabelSelected,
                onHeaderClick = { isLabelsExpanded = !isLabelsExpanded },
                onSelectAll = onSelectAllLabels,
                onUnassignedChange = onSelectUnassignedLabel,
                key = { it.id }) { label ->
                FilterItemRow(
                    filter = FilterValueUiModel(
                    id = label.id,
                    content = FilterContentType.Labeled(
                        text = label.title,
                        colorRes = remember(label.gradientColors) {
                            label.gradientColors.map { Color(it) }
                        },
                        textColorRes = remember(label.gradientTextColors) {
                            label.gradientTextColors.map { Color(it) }
                        }),
                    selected = filters.selectedLabels.contains(label.id)),
                    onCheckedChange = { filterChange ->
                        when (filterChange) {
                            is FilterChange.Selection -> {
                                val newSet =
                                    if (filters.selectedLabels.contains(filterChange.value)) {
                                        filters.selectedLabels - filterChange.value
                                    } else {
                                        filters.selectedLabels + filterChange.value
                                    }
                                onLabelsChanged(newSet)
                            }

                            else -> {}
                        }
                    })
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }

            filterArea(
                title = countryTitle,
                unassignedText = emptyText,
                items = countries.toList(),
                isExpanded = isCountriesExpanded,
                enableSelectAll = true,
                enableUnassigned = true,
                isAllSelected = filters.allCountriesSelected,
                isUnassignedSelected = filters.unassignedCountrySelected,
                onHeaderClick = { isCountriesExpanded = !isCountriesExpanded },
                onSelectAll = onSelectAllCountries,
                onUnassignedChange = onSelectUnassignedCountry,
                key = { it.isoCode }) { country ->
                FilterItemRow(
                    filter = FilterValueUiModel(
                        id = country.isoCode,
                        content = FilterContentType.Leaded(country.displayName, country.flagEmoji),
                        selected = filters.selectedCountries.contains(country.isoCode)
                    ), onCheckedChange = { filterChange ->
                        when (filterChange) {
                            is FilterChange.Selection -> {
                                val newSet =
                                    if (filters.selectedCountries.contains(filterChange.value)) {
                                        filters.selectedCountries - filterChange.value
                                    } else {
                                        filters.selectedCountries + filterChange.value
                                    }
                                onCountriesChanged(newSet)
                            }

                            else -> {}
                        }
                    })
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }

            filterArea(
                title = channelTitle,
                pagingItems = channels,
                isExpanded = isChannelsExpanded,
                enableSelectAll = true,
                isAllSelected = filters.allChannelsSelected,
                onHeaderClick = { isChannelsExpanded = !isChannelsExpanded },
                onSelectAll = onSelectAllChannels,
                key = { it.id }) { channel ->
                FilterItemRow(
                    filter = FilterValueUiModel(
                        id = channel.id,
                        content = FilterContentType.Basic(text = channel.title),
                        selected = filters.selectedChannels.contains(channel.id)
                    ),
                    onCheckedChange = { filterChange ->
                        when (filterChange) {
                            is FilterChange.Selection -> {
                                val newSet =
                                    if (filters.selectedChannels.contains(filterChange.value)) {
                                        filters.selectedChannels - filterChange.value
                                    } else {
                                        filters.selectedChannels + filterChange.value
                                    }
                                onChannelsChanged(newSet)
                            }

                            else -> {}
                        }
                    },
                )
            }

            if (filters.hasActiveFilters(isChannel)) {
                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TextButton(
                            onClick = {
                                onClearClicked()
                                dismiss()
                            }) {
                            Text(
                                stringResource(R.string.clear_filters_text),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        TextButton(
                            onClick = {
                                onApplyClicked()
                                dismiss()
                            }) {
                            Text(
                                stringResource(R.string.apply_filters_text),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}