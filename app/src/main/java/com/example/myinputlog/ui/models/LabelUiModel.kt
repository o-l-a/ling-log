package com.example.myinputlog.ui.models

import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.model.ChannelContribution
import com.example.myinputlog.data.local.model.LabelWithStats


data class LabelUiModel(
    val id: String = "",
    val title: String = "",
    val color: Long = 0xFFFFC0CB,
    val secondaryColors: List<Long> = emptyList(),
    val textColor: Long = 0xFF000000,
    val secondaryTextColors: List<Long> = emptyList(),
    val isSeparator: Boolean = false,
    val isSelected: Boolean = false,
    val totalSeconds: Long = 0L,
    val channelBreakdown: List<ChannelContribution> = emptyList()
) {
    val firstLetter: String get() = title.firstOrNull().toString().uppercase()

    val gradientColors: List<Long> get() = listOf(color) + secondaryColors

    val gradientTextColors: List<Long> get() = listOf(textColor) + secondaryTextColors
}

fun LabelEntity.toLabelUiModel(): LabelUiModel = LabelUiModel(
    id = id,
    title = title,
    color = color,
    secondaryColors = secondaryColors,
    textColor = textColor,
    secondaryTextColors = secondaryTextColors
)

fun LabelWithStats.toLabelUiModel(): LabelUiModel = LabelUiModel(
    id = label.id,
    title = label.title,
    color = label.color,
    secondaryColors = label.secondaryColors,
    textColor = label.textColor,
    secondaryTextColors = label.secondaryTextColors,
    totalSeconds = totalTimeInSeconds,
    channelBreakdown = channelBreakdown
)