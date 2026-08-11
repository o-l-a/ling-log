package com.example.myinputlog.ui.screens.label

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.toLabelUiModel
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import com.example.myinputlog.ui.navigation.LabelRoute
import com.example.myinputlog.ui.theme.ColorHelpers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val repository: StorageDataRepository
) : ViewModel() {
    sealed class LabelUiEvent {
        object NavigateBack : LabelUiEvent()
    }

    private val labelRoute = savedStateHandle.toRoute<LabelRoute>()
    private val labelId: String = sanitizeInitialLabelId(labelRoute.labelId)
    private val _isLoading = MutableStateFlow(true)
    private val _isDialogVisible = MutableStateFlow(false)
    private val _form = MutableStateFlow(LabelForm(autoCalculateTextColor = labelId.isBlank()))

    private val _uiEvent = Channel<LabelUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()


    val labelUiState: StateFlow<LabelUiState> = combine(
        _form, _isLoading, _isDialogVisible
    ) { form, loading, dialogVisible ->
        if (loading) {
            LabelUiState.Loading
        } else {
            val textColor = if (form.autoCalculateTextColor) {
                form.previewColor?.let { ColorHelpers.longToHex(ColorHelpers.calculateFontColor(it)) }
                    ?: ""
            } else {
                form.textColorHex
            }
            LabelUiState.Success(
                label = form.copy(textColorHex = textColor),
                isFormValid = validateFields(form),
                isDialogVisible = dialogVisible
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LabelUiState.Loading
    )

    init {
        loadLabel()
    }

    private fun loadLabel() {
        viewModelScope.launch {
            if (labelId.isNotBlank()) {
                val label = repository.getLabelById(labelId)
                if (label != null) {
                    _form.value = label.toLabelUiModel().toLabelForm()
                }
            }
            _isLoading.value = false
        }
    }

    fun deleteLabel() {
        toggleDialogVisibility(false)
        viewModelScope.launch {
            repository.deleteLabel(labelId)
            _uiEvent.send(LabelUiEvent.NavigateBack)
        }
    }

    fun saveLabel() {
        val currentState = labelUiState.value as? LabelUiState.Success ?: return
        val form = currentState.label
        viewModelScope.launch {
            val labelEntity = form.toLabelEntity()
            repository.saveLabel(labelEntity)
            _uiEvent.send(LabelUiEvent.NavigateBack)
        }
    }

    private fun validateFields(label: LabelForm): Boolean {
        return label.title.isNotBlank() && label.previewColor != null && label.previewTextColor != null
    }

    fun toggleDialogVisibility(visible: Boolean) {
        _isDialogVisible.value = visible
    }

    fun onTitleChange(newTitle: String) {
        _form.update { it.copy(title = newTitle) }
    }

    fun onColorChange(newColor: String) {
        _form.update { it.copy(colorHex = newColor) }
    }

    fun onTextColorChange(newColor: String) {
        _form.update { it.copy(textColorHex = newColor) }
    }

    fun onAutoCalculateChange(checked: Boolean) {
        _form.update { it.copy(autoCalculateTextColor = checked) }
    }

    private fun sanitizeInitialLabelId(id: String): String {
        return if (id == DEFAULT_ID.toString()) {
            ""
        } else {
            id
        }
    }
}