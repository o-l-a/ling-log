package com.example.myinputlog.ui.models

import androidx.compose.ui.graphics.Color

sealed class FilterContentType {
    data class Basic(val text: String) : FilterContentType()

    data class Leaded(val text: String, val leadingText: String) : FilterContentType()

    data class Labeled(
        val text: String, val colorRes: Color, val textColorRes: Color
    ) : FilterContentType()
}

data class FilterValueUiModel(
    val id: String,
    val content: FilterContentType,
    val selected: Boolean,
    val isToggleType: Boolean = false
)
