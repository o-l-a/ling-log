package com.example.myinputlog.ui.screens.channel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.R
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.LabelUiModel
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val _form = MutableStateFlow(ChannelForm())
    private val _metadata = MutableStateFlow(ChannelMetadata())
    private val _loadingState = MutableStateFlow<ChannelLoadState>(ChannelLoadState.Loading)
    private val _isEditStarted = MutableStateFlow(false)

    private val _uiEvent = Channel<ChannelUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    @OptIn(FlowPreview::class)
    val suggestions: StateFlow<List<LabelUiModel>> = combine(
        _form.map { it.searchQuery }.distinctUntilChanged().debounce(100),
        _form.map { it.selectedLabels }.distinctUntilChanged(),
        _metadata.map { it.allLabels }.distinctUntilChanged()
    ) { query, selected, all ->
        if (query.isEmpty()) emptyList()
        else {
            all.filter { label ->
                label.title.contains(
                    query, ignoreCase = true
                ) && selected.none { it.id == label.id }
            }.sortedWith(compareByDescending<LabelUiModel> {
                it.title.startsWith(
                    query, ignoreCase = true
                )
            }.thenBy { it.title.lowercase() })
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channelUiState: StateFlow<ChannelUiState> = combine(
        _form, _metadata, _loadingState, _isEditStarted, suggestions
    ) { form, meta, loading, editStarted, currentSuggestions ->
        if (loading is ChannelLoadState.Loading) return@combine ChannelUiState.Loading
        ChannelUiState.Success(
            metadata = meta,
            form = form,
            suggestions = currentSuggestions.toSet(),
            uiFlags = ChannelUiFlags(
                isDeleteEnabled = meta.totalVideoCount == 0L,
                isEditStarted = editStarted,
                isFormValid = form.selectedLabels != meta.initialLabels
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChannelUiState.Loading)

    init {
        loadChannelAndLabels()
    }

    private fun loadChannelAndLabels() {
        viewModelScope.launch {
            val allLabels =
                storageDataRepository.getAllLabelsAsSet().map { it.toLabelUiModel() }.toSet()
            val channel = storageDataRepository.getChannel(channelId)?.toChannelMetadata(allLabels)
            if (channel != null) {
                _metadata.value = channel
                _form.update { it.copy(selectedLabels = channel.initialLabels) }
                _loadingState.value = ChannelLoadState.Success
            } else {
                _loadingState.value = ChannelLoadState.Error
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _form.update { it.copy(searchQuery = newQuery) }
        _isEditStarted.value = true
    }

    fun startEdit() {
        _isEditStarted.value = true
    }

    fun addLabel(label: LabelUiModel) {
        val currentLabels = _form.value.selectedLabels
        if (label !in currentLabels) {
            _form.update {
                it.copy(selectedLabels = (currentLabels + label), searchQuery = "")
            }
        }
    }

    fun removeLabel(label: LabelUiModel) {
        _form.update {
            it.copy(
                selectedLabels = it.selectedLabels - label
            )
        }
    }

    fun saveChannel() {
        val currentState = channelUiState.value as? ChannelUiState.Success ?: return
        val channel = currentState.metadata
        val selectedLabels = currentState.form.selectedLabels
        viewModelScope.launch {
            try {
                val channelEntity = channel.toChannelEntity()
                storageDataRepository.saveChannel(
                    channel = channelEntity,
                    labelIds = selectedLabels.map { it.id },
                    syncLabelsToVideos = currentState.form.syncLabelsToVideos
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