package com.example.myinputlog.ui.screens.channel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.R
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.toChannelUiModel
import com.example.myinputlog.ui.models.toLabelUiModel
import com.example.myinputlog.ui.navigation.ChannelRoute
import com.example.myinputlog.ui.screens.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val storageDataRepository: StorageDataRepository
) : ViewModel() {
    sealed class ChannelUiEvent {
        data class ShowSnackbar(val message: UiText) : ChannelUiEvent()
        object NavigateBack : ChannelUiEvent()
    }

    private val channelRoute = savedStateHandle.toRoute<ChannelRoute>()
    private val channelId = channelRoute.channelId

    private val _channelData = MutableStateFlow(ChannelUiModel())
    private val _loadingState = MutableStateFlow<ChannelLoadState>(ChannelLoadState.Loading)

    private val _searchQuery = MutableStateFlow("")
    private val _allLabels = MutableStateFlow<List<LabelUiModel>>(emptyList())

    private val _initialLabels = MutableStateFlow<Set<LabelUiModel>>(emptySet())

    private val _uiEvent = Channel<ChannelUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    @OptIn(FlowPreview::class)
    val suggestions: StateFlow<List<LabelUiModel>> =
        _searchQuery.debounce(100).combine(_allLabels) { query, all -> query to all }
            .combine(_channelData) { (query, all), data ->
                if (query.isEmpty()) emptyList()
                else {
                    all.filter { label ->
                        label.title.contains(
                            query, ignoreCase = true
                        ) && data.defaultLabels.none { it.id == label.id }
                    }.sortedWith(compareByDescending<LabelUiModel> {
                        it.title.startsWith(query, ignoreCase = true)
                    }.thenBy { it.title.lowercase() })
                }
            }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channelUiState: StateFlow<ChannelUiState> = combine(
        _channelData, _loadingState, _searchQuery, suggestions, _initialLabels
    ) { channel, loading, query, suggestions, initialLabels ->
        val hasChanged = channel.defaultLabels.toSet() != initialLabels
        when (loading) {
            is ChannelLoadState.Loading -> ChannelUiState.Loading
            is ChannelLoadState.Error -> ChannelUiState.Error
            else -> {
                ChannelUiState.Success(
                    channelUiModel = channel,
                    searchQuery = query,
                    suggestions = suggestions.toSet(),
                    isFormValid = hasChanged
                )
            }
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
                _initialLabels.value = channel.defaultLabels.toSet()
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

    fun saveChannel() {
        val currentState = channelUiState.value as? ChannelUiState.Success ?: return
        val channel = currentState.channelUiModel
        viewModelScope.launch {
            try {
                val channelEntity = channel.toChannelEntity()
                storageDataRepository.saveChannel(
                    channel = channelEntity,
                    labelIds = channel.defaultLabels.toList().map { it.id },
                    syncLabelsToVideos = currentState.syncLabelsToVideos
                )
                _uiEvent.send(ChannelUiEvent.NavigateBack)
            } catch (e: Exception) {
                Log.e(TAG, "Save failed", e)
                _uiEvent.send(ChannelUiEvent.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
            }
        }
    }

    companion object {
        private const val TAG = "ChannelViewModel"
    }
}