package com.example.myinputlog.ui.models

import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels

data class ChannelUiModel(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val customUrl: String? = null,
    val country: String? = null,
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultLabels: Set<LabelUiModel> = emptySet(),
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)

fun ChannelWithStatsAndLabels.toChannelUiModel(): ChannelUiModel = ChannelUiModel(
    id = channel.id,
    courseId = channel.courseId,
    title = channel.title,
    customUrl = channel.customUrl,
    country = channel.country,
    thumbnailDefaultUrl = channel.thumbnailDefaultUrl,
    thumbnailMediumUrl = channel.thumbnailMediumUrl,
    thumbnailHighUrl = channel.thumbnailHighUrl,
    totalTimeInSeconds = totalTimeInSeconds,
    totalVideoCount = totalVideoCount,
    defaultLabels = labels.map { it.toLabelUiModel() }
        .toSortedSet(compareBy { it.title.lowercase() })
)