package com.example.myinputlog.ui.screens.channel

import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.ui.models.ChannelUiModel

fun ChannelUiModel.toChannelEntity(): ChannelEntity = ChannelEntity(
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