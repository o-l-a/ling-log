package com.example.myinputlog.ui.models

import com.example.myinputlog.data.local.entities.LabelEntity


data class LabelUiModel(
    val id: String = "",
    val title: String = "",
    val color: Long = 0xFFFFC0CB,
    val textColor: Long = 0xFF000000,
    val isSeparator: Boolean = false,
    val isSelected: Boolean = false
) {
    val firstLetter: String get() = title.firstOrNull().toString().uppercase()
}

fun LabelEntity.toLabelUiModel(): LabelUiModel = LabelUiModel(
    id = id, title = title, color = color, textColor = textColor
)