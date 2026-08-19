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
    val colorsHex: List<String> = listOf("FFFFC0CB"),
    val activeColorIndex: Int = 0,
    val textColorsHex: List<String> = listOf("FF000000"),
    val activeTextColorIndex: Int = 0,
    val autoCalculateTextColor: Boolean = false
) {
    val previewColors: List<Long>
        get() = colorsHex.mapNotNull { ColorHelpers.hexToLong(it) }

    val previewTextColors: List<Long>
        get() = textColorsHex.mapNotNull { ColorHelpers.hexToLong(it) }

    val activeColorHex: String
        get() = colorsHex.getOrElse(activeColorIndex) { colorsHex.firstOrNull() ?: "" }

    val activeTextColorHex: String
        get() = textColorsHex.getOrElse(activeTextColorIndex) { textColorsHex.firstOrNull() ?: "" }
}