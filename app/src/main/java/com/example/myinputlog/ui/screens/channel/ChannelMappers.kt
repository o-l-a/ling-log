package com.example.myinputlog.ui.screens.channel

import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels
import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.toLabelUiModel

fun ChannelMetadata.toChannelEntity(): ChannelEntity = ChannelEntity(
    id = id,
    title = title,
    courseId = courseId,
    customUrl = customUrl,
    country = country,
    thumbnailDefaultUrl = thumbnailDefaultUrl,
    thumbnailMediumUrl = thumbnailMediumUrl,
    thumbnailHighUrl = thumbnailHighUrl,
    isDeleted = false,
    lastUpdated = System.currentTimeMillis(),
)

fun ChannelWithStatsAndLabels.toChannelMetadata(
    allLabels: Set<LabelUiModel>, availableLanguages: List<CountryUiModel>
): ChannelMetadata = ChannelMetadata(
    id = channel.id,
    title = channel.title,
    courseId = channel.courseId,
    customUrl = channel.customUrl,
    country = channel.country,
    availableLanguages = availableLanguages,
    thumbnailDefaultUrl = channel.thumbnailDefaultUrl,
    thumbnailMediumUrl = channel.thumbnailMediumUrl,
    thumbnailHighUrl = channel.thumbnailHighUrl,
    initialLabels = labels.map { it.toLabelUiModel() }
        .toSortedSet(compareBy { it.title.lowercase() }),
    allLabels = allLabels,
    totalTimeInSeconds = totalTimeInSeconds,
    totalVideoCount = totalVideoCount
)