package com.example.myinputlog.ui.screens.label

import com.example.myinputlog.ui.theme.ColorHelpers


sealed interface LabelUiState {
    data object Loading : LabelUiState
    data object Error : LabelUiState
    data class Success(
        val label: LabelForm = LabelForm(),
        val isFormValid: Boolean = false,
        val isDialogVisible: Boolean = false
    ) : LabelUiState
}

data class LabelForm(
    val id: String = "",
    val title: String = "",
    val colorHex: String = "FFFFC0CB",
    val textColorHex: String = "FF000000",
    val autoCalculateTextColor: Boolean = false
) {
    val previewColor: Long?
        get() = ColorHelpers.hexToLong(colorHex)

    val previewTextColor: Long?
        get() = ColorHelpers.hexToLong(textColorHex)
}