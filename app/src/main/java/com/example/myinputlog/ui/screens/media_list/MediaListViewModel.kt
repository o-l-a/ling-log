package com.example.myinputlog.ui.screens.media_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.models.mapToCourseUiModel
import com.example.myinputlog.ui.models.toCourseUiModel
import com.example.myinputlog.ui.models.toLabelUiModel
import com.example.myinputlog.ui.screens.common.composable.input.FilterChange
import com.example.myinputlog.ui.screens.common.ext.asStartOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val repository: StorageDataRepository,
) : ViewModel() {
    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    private val _draftFilters = MutableStateFlow(MediaFilters())
    private val _appliedFilters = MutableStateFlow(MediaFilters())
    private val _channelRanking = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _allLabels: Flow<List<LabelUiModel>> =
        repository.labels.map { list -> list.map { it.toLabelUiModel() } }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val videoFlow = combine(currentCourseId, _appliedFilters, ::Pair).debounce(300L)
        .flatMapLatest { (courseId, filters) ->
            repository.videoPagingFlow(courseId, filters).insertHeaderAndSeparators()
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val channelFlow =
        combine(currentCourseId, _appliedFilters, _channelRanking, ::ChannelQuery).debounce(300L)
            .flatMapLatest { query ->
                repository.channelPagingFlow(query.courseId, query.filters, query.ranking)
            }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val filterChannelFlow = currentCourseId.flatMapLatest { courseId ->
        repository.channelPagingFlow(courseId, MediaFilters())
    }.cachedIn(viewModelScope)

    val mediaListUiState: StateFlow<MediaListUiState> = combine(
        repository.courses, currentCourseId, _draftFilters, _allLabels
    ) { courses, id, filters, labels ->

        when {
            courses.isEmpty() -> MediaListUiState.Empty

            else -> {
                val current = courses.find { it.course.id == id } ?: courses.first()
                val courseHeader = mapToCourseUiModel(current.toCourseUiModel())

                MediaListUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses.map { it.toCourseUiModel() },
                    filters = filters,
                    allLabels = labels.toSet()
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaListUiState.Loading
    )

    init {
        viewModelScope.launch {
            _channelRanking.value = repository.getChannelGlobalRanking()
        }
    }

    fun updateSearchQuery(query: String) {
        _draftFilters.update { it.copy(searchQuery = query) }
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

    fun onSelectAllLabelsChange(filterChange: FilterChange) {
        viewModelScope.launch {
            when (filterChange) {
                is FilterChange.Toggle -> {
                    if (filterChange.isChecked) {
                        val allLabels = repository.getAllLabelsAsSet().map { it.id }
                        _draftFilters.update {
                            it.copy(
                                selectedLabels = allLabels.toSet(), allLabelsSelected = true
                            )
                        }
                    } else {
                        _draftFilters.update {
                            it.copy(
                                selectedLabels = emptySet(), allLabelsSelected = false
                            )
                        }
                    }
                }

                else -> {}
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

    fun clearFilters() {
        _appliedFilters.value = MediaFilters()
        _draftFilters.value = MediaFilters()
    }

    fun applyFilters() {
        _appliedFilters.value = _draftFilters.value
    }

    private data class ChannelQuery(
        val courseId: String, val filters: MediaFilters, val ranking: Map<String, Int>
    )

    companion object {
        private const val TAG = "MediaListViewModel"
    }
}

fun Flow<PagingData<VideoUiModel>>.insertHeaderAndSeparators(): Flow<PagingData<VideoUiModel>> {
    return this.map {
        it.insertSeparators { before: VideoUiModel?, after: VideoUiModel? ->
            when {
                before == null && after != null -> {
                    VideoUiModel(watchedOn = after.watchedOn)
                }

                before == null || after == null -> {
                    null
                }

                before.watchedOn.asStartOfDay() != after.watchedOn.asStartOfDay() -> {
                    VideoUiModel(watchedOn = after.watchedOn)
                }

                else -> {
                    null
                }
            }
        }
    }
}