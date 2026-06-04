package com.example.myinputlog.data.local.model

import com.example.myinputlog.data.local.entities.ChannelEntity

data class ChannelWithLabelIds(
    val channel: ChannelEntity, val labelIds: List<String>
)