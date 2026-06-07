package com.example.myinputlog.ui.screens.channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.toChannelUiModel
import com.example.myinputlog.ui.models.toLabelUiModel
import com.example.myinputlog.ui.navigation.ChannelRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val storageDataRepository: StorageDataRepository
) : ViewModel() {
    private val channelRoute = savedStateHandle.toRoute<ChannelRoute>()
    private val channelId = channelRoute.channelId

    private val _channelData = MutableStateFlow(ChannelUiModel())
    private val _loadingState = MutableStateFlow<ChannelLoadState>(ChannelLoadState.Loading)

    private val _searchQuery = MutableStateFlow("")
    private val _allLabels = MutableStateFlow<List<LabelUiModel>>(emptyList())

    private val _suggestions = combine(_searchQuery, _channelData, _allLabels) { query, data, all ->
        if (query.isEmpty()) return@combine emptyList()

        all.filter { systemLabel ->
            val isNotSelected = data.defaultLabels.none { it.id == systemLabel.id }
            val matchesQuery = systemLabel.title.contains(query, ignoreCase = true)
            isNotSelected && matchesQuery
        }
    }

    val channelUiState: StateFlow<ChannelUiState> = combine(
        _channelData, _loadingState, _searchQuery, _suggestions
    ) { meta, state, query, suggestions ->
        when (state) {
            is ChannelLoadState.Loading -> ChannelUiState.Loading
            is ChannelLoadState.Error -> ChannelUiState.Error
            is ChannelLoadState.Success -> ChannelUiState.Success(
                channelUiModel = meta,
                channelLoadState = state,
                searchQuery = query,
                suggestions = suggestions.toSet()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChannelUiState.Loading
    )

    init {
        loadChannelAndLabels()
    }

    private fun loadChannelAndLabels() {
        viewModelScope.launch {
            val channel = storageDataRepository.getChannel(channelId)?.toChannelUiModel()
            val allLabels = storageDataRepository.getAllLabelsAsSet().map { it.toLabelUiModel() }
            if (channel != null) {
                _allLabels.value = allLabels
                _channelData.value = channel
                _loadingState.value = ChannelLoadState.Success
            } else {
                _loadingState.value = ChannelLoadState.Error
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addLabel(label: LabelUiModel) {
        val currentLabels = _channelData.value.defaultLabels
        if (label !in currentLabels) {
            _channelData.value = _channelData.value.copy(
                defaultLabels = currentLabels + label
            )
        }
        _searchQuery.value = ""
    }

    fun removeLabel(label: LabelUiModel) {
        _channelData.value = _channelData.value.copy(
            defaultLabels = _channelData.value.defaultLabels - label
        )
    }
}