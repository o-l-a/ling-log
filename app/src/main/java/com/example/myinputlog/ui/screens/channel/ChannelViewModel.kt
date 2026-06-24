package com.example.myinputlog.ui.screens.channel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.R
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.toCountryUiModel
import com.example.myinputlog.ui.models.toCountryUiModelOrNull
import com.example.myinputlog.ui.models.toLabelUiModel
import com.example.myinputlog.ui.navigation.ChannelRoute
import com.example.myinputlog.ui.screens.common.UiText
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
    savedStateHandle: SavedStateHandle, private val repository: StorageDataRepository
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
    private val _isDialogVisible = MutableStateFlow(false)

    private val _uiEvent = Channel<ChannelUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    @OptIn(FlowPreview::class)
    val suggestions: StateFlow<List<LabelUiModel>> =
        combine(
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

    val uiFlags: StateFlow<ChannelUiFlags> = combine(
        _metadata, _isEditStarted, _isDialogVisible, _form
    ) { meta, editStarted, isDelete, form ->
        ChannelUiFlags(
            isDeleteEnabled = meta.totalVideoCount == 0L,
            isEditStarted = editStarted,
            isFormValid = validateForm(form, meta) && editStarted,
            isDialogVisible = isDelete
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChannelUiFlags())


    val channelUiState: StateFlow<ChannelUiState> = combine(
        _form, _metadata, _loadingState, uiFlags, suggestions
    ) { form, meta, loading, uiFlags, currentSuggestions ->
        if (loading is ChannelLoadState.Loading) return@combine ChannelUiState.Loading
        ChannelUiState.Success(
            metadata = meta,
            form = form,
            suggestions = currentSuggestions.toSet(),
            uiFlags = uiFlags
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChannelUiState.Loading)

    init {
        loadChannelAndLabels()
    }

    private fun loadChannelAndLabels() {
        viewModelScope.launch {
            val allLabels = repository.getAllLabelsAsSet().map { it.toLabelUiModel() }.toSet()
            val channel = repository.getChannel(channelId)
            if (channel != null) {
                val languages = repository.getCountriesForCourse(channel.channel.courseId)
                    .map { it.toCountryUiModel() }
                val channelWithMetadata = channel.toChannelMetadata(allLabels, languages)
                _metadata.value = channelWithMetadata
                _form.update {
                    it.copy(
                        selectedLabels = channelWithMetadata.initialLabels,
                        defaultLanguage = channel.channel.defaultLanguage.toCountryUiModelOrNull()
                    )
                }
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

    fun updateDefaultLanguage(newLanguage: CountryUiModel?) {
        _form.update {
            it.copy(defaultLanguage = newLanguage)
        }
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

    fun toggleDeleteDialogVisibility(visible: Boolean) {
        _isDialogVisible.value = visible
    }

    fun onSyncLabelsChange(checked: Boolean) {
        _form.update { it.copy(syncLabelsToVideos = checked) }
    }

    fun removeLabel(label: LabelUiModel) {
        _form.update {
            it.copy(
                selectedLabels = it.selectedLabels - label
            )
        }
    }

    fun validateForm(form: ChannelForm, metadata: ChannelMetadata): Boolean {
        val labelsChanged = form.selectedLabels != metadata.initialLabels
        val languageChanged = form.defaultLanguage != metadata.initialDefaultLanguage
        return labelsChanged || languageChanged
    }

    fun saveChannel() {
        val currentState = channelUiState.value as? ChannelUiState.Success ?: return
        val channel = currentState.metadata
        val defaultLanguage = currentState.form.defaultLanguage
        val selectedLabels = currentState.form.selectedLabels
        val initialLabels = currentState.metadata.initialLabels
        val labelsChanged = selectedLabels != initialLabels
        Log.d(TAG, "Labels have ${if (!labelsChanged) "not " else ""}changed.")
        viewModelScope.launch {
            try {
                val channelEntity =
                    channel.toChannelEntity().copy(defaultLanguage = defaultLanguage?.isoCode)
                repository.saveChannel(
                    channel = channelEntity,
                    labelIds = selectedLabels.map { it.id },
                    initialLabelIds = initialLabels.map { it.id },
                    syncLabelsToVideos = currentState.form.syncLabelsToVideos && labelsChanged
                )
                _uiEvent.send(ChannelUiEvent.NavigateBack)
            } catch (e: Exception) {
                Log.e(TAG, "Save failed", e)
                _uiEvent.send(ChannelUiEvent.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
            }
        }
    }

    fun deleteChannel() {
        toggleDeleteDialogVisibility(false)
        viewModelScope.launch {
            try {
                repository.deleteChannel(channelId)
                _uiEvent.send(ChannelUiEvent.NavigateBack)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _uiEvent.send(ChannelUiEvent.ShowSnackbar(UiText.StringResource(R.string.channel_delete_error)))
            }
        }
    }

    companion object {
        private const val TAG = "ChannelViewModel"
    }
}