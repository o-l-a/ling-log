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
            val textColors = if (form.autoCalculateTextColor) {
                val primaryBg = form.previewColors.firstOrNull() ?: 0xFFFFC0CB
                val calculatedTextColor =
                    ColorHelpers.longToHex(ColorHelpers.calculateFontColor(primaryBg))
                listOf(calculatedTextColor)
            } else {
                form.textColorsHex
            }

            val updatedForm = form.copy(textColorsHex = textColors)

            LabelUiState.Success(
                label = updatedForm,
                isFormValid = validateFields(updatedForm),
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
        return label.title.isNotBlank() && label.previewColors.size == label.colorsHex.size && label.previewTextColors.size == label.textColorsHex.size
    }

    fun toggleDialogVisibility(visible: Boolean) {
        _isDialogVisible.value = visible
    }

    fun onTitleChange(newTitle: String) {
        _form.update { it.copy(title = newTitle) }
    }

    fun onAutoCalculateChange(checked: Boolean) {
        _form.update { it.copy(autoCalculateTextColor = checked) }
    }

    fun onActiveColorHexChange(newHex: String) {
        _form.update { form ->
            val updatedList = form.colorsHex.toMutableList().apply {
                if (form.activeColorIndex in indices) {
                    this[form.activeColorIndex] = newHex
                }
            }
            form.copy(colorsHex = updatedList)
        }
    }

    fun onSelectActiveColorIndex(index: Int) {
        _form.update { it.copy(activeColorIndex = index.coerceIn(0, it.colorsHex.lastIndex)) }
    }

    fun onAddBackgroundColor() {
        _form.update { form ->
            if (form.colorsHex.size >= 20) return@update form
            val newColor = form.colorsHex.lastOrNull() ?: "FFFFFFFF"
            val updatedList = form.colorsHex + newColor
            form.copy(
                colorsHex = updatedList, activeColorIndex = updatedList.lastIndex
            )
        }
    }

    fun onRemoveBackgroundColor(index: Int) {
        _form.update { form ->
            if (form.colorsHex.size <= 1) return@update form
            val updatedList = form.colorsHex.toMutableList().apply { removeAt(index) }
            val newActiveIndex = form.activeColorIndex.coerceAtMost(updatedList.lastIndex)
            form.copy(
                colorsHex = updatedList, activeColorIndex = newActiveIndex
            )
        }
    }

    fun onActiveTextColorHexChange(newHex: String) {
        _form.update { form ->
            val updatedList = form.textColorsHex.toMutableList().apply {
                if (form.activeTextColorIndex in indices) {
                    this[form.activeTextColorIndex] = newHex
                }
            }
            form.copy(textColorsHex = updatedList)
        }
    }

    fun onSelectActiveTextColorIndex(index: Int) {
        _form.update {
            it.copy(
                activeTextColorIndex = index.coerceIn(
                    0, it.textColorsHex.lastIndex
                )
            )
        }
    }

    fun onAddTextColor() {
        _form.update { form ->
            if (form.textColorsHex.size >= 20) return@update form
            val newColor = form.textColorsHex.lastOrNull() ?: "FF000000"
            val updatedList = form.textColorsHex + newColor
            form.copy(
                textColorsHex = updatedList, activeTextColorIndex = updatedList.lastIndex
            )
        }
    }

    fun onRemoveTextColor(index: Int) {
        _form.update { form ->
            if (form.textColorsHex.size <= 1) return@update form
            val updatedList = form.textColorsHex.toMutableList().apply { removeAt(index) }
            val newActiveIndex = form.activeTextColorIndex.coerceAtMost(updatedList.lastIndex)
            form.copy(
                textColorsHex = updatedList, activeTextColorIndex = newActiveIndex
            )
        }
    }

    private fun sanitizeInitialLabelId(id: String): String {
        return if (id == DEFAULT_ID.toString()) {
            ""
        } else {
            id
        }
    }
}