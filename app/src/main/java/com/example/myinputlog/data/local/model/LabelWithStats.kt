package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import androidx.room.Ignore
import com.example.myinputlog.data.local.entities.LabelEntity

data class LabelWithStats(
    @Embedded val label: LabelEntity,
    val totalTimeInSeconds: Long = 0L,
    @Ignore val channelBreakdown: List<ChannelContribution> = emptyList()
) {
    constructor(label: LabelEntity, totalTimeInSeconds: Long) : this(
        label, totalTimeInSeconds, emptyList()
    )
}