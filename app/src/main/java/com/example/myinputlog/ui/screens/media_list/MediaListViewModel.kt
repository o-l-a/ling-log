package com.example.myinputlog.ui.screens.media_list

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.toCountryUiModel
import com.example.myinputlog.ui.models.toLabelUiModel
import com.example.myinputlog.ui.navigation.MediaListRoute
import com.example.myinputlog.ui.screens.common.composable.input.FilterChange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val repository: StorageDataRepository, savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val initialTargetDate: Long? = savedStateHandle.toRoute<MediaListRoute>().targetDate
    private var hasHandledInitialDate = false

    private val _scrollToIndex = MutableStateFlow<Int?>(null)
    val scrollToIndex: StateFlow<Int?> = _scrollToIndex.asStateFlow()

    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    private val _draftFilters = MutableStateFlow(MediaFilters())
    private val _appliedFilters = MutableStateFlow(MediaFilters())
    private val _appliedChannelSort = MutableStateFlow(SortOptions.DEFAULT)
    private val _appliedVideoSort = MutableStateFlow(SortOptions.DEFAULT)
    private val _channelRanking = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _allLabels: Flow<List<LabelUiModel>> =
        repository.labels.map { list -> list.map { it.toLabelUiModel() } }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _allCountries: Flow<List<CountryUiModel>> =
        currentCourseId.flatMapLatest { courseId ->
            repository.getCountriesFlow(courseId)
        }.map { countries ->
            countries.map { it.toCountryUiModel() }.sortedBy { it.displayName }
        }.flowOn(Dispatchers.Default)

    @OptIn(FlowPreview::class)
    private val debouncedFilters =
        _appliedFilters.scan(_appliedFilters.value.searchQuery to _appliedFilters.value) { (oldQuery, current), next ->
            current.searchQuery to next
        }.debounce { (oldQuery, next) ->
            if (next.searchQuery != oldQuery) 300L else 0L
        }.map { it.second }.distinctUntilChanged().flowOn(Dispatchers.Default)


    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val videoFlow = combine(
        currentCourseId, debouncedFilters, _appliedVideoSort, ::VideoQuery
    ).flatMapLatest { query ->
        var initialKey: Int? = null

        if (!hasHandledInitialDate && initialTargetDate != null && query.courseId.isNotBlank()) {
            hasHandledInitialDate = true
            val offset = repository.getVideoOffsetForDate(query.courseId, initialTargetDate)
            initialKey = offset
            _scrollToIndex.value = offset
        }
        repository.videoPagingFlow(query.courseId, query.filters, query.sort, initialKey)
    }.flowOn(Dispatchers.Default).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val channelFlow = combine(
        currentCourseId, debouncedFilters, _appliedChannelSort, _channelRanking, ::ChannelQuery
    ).flatMapLatest { query ->
        repository.channelPagingFlow(query.courseId, query.filters, query.sort, query.ranking)
    }.flowOn(Dispatchers.Default).cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val videoCountFlow: Flow<Int> = combine(
        currentCourseId, debouncedFilters, ::Pair
    ).flatMapLatest { (id, filters) ->
        repository.videoCountFlow(id, filters)
    }.distinctUntilChanged().flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val channelCountFlow: Flow<Int> = combine(
        currentCourseId, debouncedFilters, ::Pair
    ).flatMapLatest { (id, filters) ->
        repository.channelCountFlow(id, filters)
    }.distinctUntilChanged().flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val filterChannelFlow = currentCourseId.flatMapLatest { courseId ->
        repository.channelPagingFlow(courseId, MediaFilters(), SortOptions.CHANNEL_TITLE_ASC)
            .flowOn(Dispatchers.Default)
    }.cachedIn(viewModelScope)

    private val mediaCountsFlow = combine(videoCountFlow, channelCountFlow, ::Pair)
    private val filterOptionsFlow = combine(_allLabels, _allCountries, ::Pair)
    private val appliedSortFlow = combine(_appliedChannelSort, _appliedVideoSort, ::Pair)

    val mediaListUiState: StateFlow<MediaListUiState> = combine(
        currentCourseId, _draftFilters, appliedSortFlow, filterOptionsFlow, mediaCountsFlow
    ) { id, filters, (cSort, vSort), (labels, countries), (vCount, cCount) ->

        when {
            id.isBlank() -> MediaListUiState.Empty

            else -> {
                MediaListUiState.Success(
                    currentCourseId = id,
                    filters = filters,
                    allLabels = labels.toSet(),
                    allCountries = countries.toSet(),
                    appliedChannelSort = cSort,
                    appliedVideoSort = vSort,
                    videoCount = vCount,
                    channelCount = cCount
                )
            }
        }
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaListUiState.Loading
    )

    init {
        viewModelScope.launch {
            _channelRanking.value = repository.getChannelGlobalRanking()
            _appliedChannelSort.value = repository.channelSortDefault.first()
            _appliedVideoSort.value = repository.videoSortDefault.first()
        }
    }

    fun updateSearchQuery(query: String) {
        _appliedFilters.update { it.copy(searchQuery = query) }
        Log.d(TAG, "Channel ranking: ${_channelRanking.value}")
    }

    fun updateSelectedChannels(channelIds: Set<String>) {
        _draftFilters.update {
            it.copy(
                selectedChannels = channelIds, allChannelsSelected = false
            )
        }
    }

    fun updateSelectedLabels(labelIds: Set<String>) {
        _draftFilters.update {
            it.copy(
                selectedLabels = labelIds, allLabelsSelected = false
            )
        }
    }

    fun updateSelectedCountries(countryIds: Set<String>) {
        _draftFilters.update {
            it.copy(
                selectedCountries = countryIds, allCountriesSelected = false
            )
        }
    }

    fun onSelectAllLabelsChange(filterChange: FilterChange) {
        viewModelScope.launch {
            when (filterChange) {
                is FilterChange.Toggle -> {
                    if (filterChange.isChecked) {
                        val allLabels = repository.getAllLabelsAsSet().map { it.id }
                        _draftFilters.update {
                            it.copy(
                                selectedLabels = allLabels.toSet(),
                                unassignedLabelSelected = true,
                                allLabelsSelected = true

                            )
                        }
                    } else {
                        _draftFilters.update {
                            it.copy(
                                selectedLabels = emptySet(),
                                unassignedLabelSelected = false,
                                allLabelsSelected = false
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun onUnassignedLabelsChange(filterChange: FilterChange) {
        if (filterChange is FilterChange.Toggle) {
            _draftFilters.update {
                it.copy(
                    unassignedLabelSelected = filterChange.isChecked, allLabelsSelected = false
                )
            }
        }
    }

    fun onSelectAllChannelsChange(filterChange: FilterChange) {
        viewModelScope.launch {
            when (filterChange) {
                is FilterChange.Toggle -> {
                    if (filterChange.isChecked) {
                        val allChannels = repository.getChannelIdsForCourse(currentCourseId.value)
                        _draftFilters.update {
                            it.copy(
                                selectedChannels = allChannels.toSet(), allChannelsSelected = true
                            )
                        }
                    } else {
                        _draftFilters.update {
                            it.copy(
                                selectedChannels = emptySet(), allChannelsSelected = false
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun onSelectAllCountriesChange(filterChange: FilterChange) {
        viewModelScope.launch {
            when (filterChange) {
                is FilterChange.Toggle -> {
                    if (filterChange.isChecked) {
                        val allCountries = repository.getCountriesForCourse(currentCourseId.value)
                        _draftFilters.update {
                            it.copy(
                                selectedCountries = allCountries.toSet(),
                                unassignedCountrySelected = true,
                                allCountriesSelected = true
                            )
                        }
                    } else {
                        _draftFilters.update {
                            it.copy(
                                selectedCountries = emptySet(),
                                unassignedCountrySelected = false,
                                allCountriesSelected = false
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun onUnassignedCountriesChange(filterChange: FilterChange) {
        if (filterChange is FilterChange.Toggle) {
            _draftFilters.update {
                it.copy(
                    unassignedCountrySelected = filterChange.isChecked, allCountriesSelected = false
                )
            }
        }
    }

    fun clearFilters() {
        _appliedFilters.value = MediaFilters()
        _draftFilters.value = MediaFilters()
    }

    fun applyFilters() {
        _appliedFilters.value = _draftFilters.value
    }

    fun onChannelSortChange(newSort: SortOptions) {
        _appliedChannelSort.value = newSort
    }

    fun onVideoSortChange(newSort: SortOptions) {
        _appliedVideoSort.value = newSort
    }

    fun onScrollConsumed() {
        _scrollToIndex.value = null
    }

    private data class VideoQuery(
        val courseId: String, val filters: MediaFilters, val sort: SortOptions
    )

    private data class ChannelQuery(
        val courseId: String,
        val filters: MediaFilters,
        val sort: SortOptions,
        val ranking: Map<String, Int>
    )

    companion object {
        private const val TAG = "MediaListViewModel"
    }
}
