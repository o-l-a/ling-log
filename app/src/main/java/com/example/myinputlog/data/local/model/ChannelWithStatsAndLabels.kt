package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.ChannelLabelCrossRef
import com.example.myinputlog.data.local.entities.LabelEntity

class ChannelWithStatsAndLabels(
    @Embedded val channel: ChannelEntity,
    @Relation(
        parentColumn = "id", entityColumn = "id", associateBy = Junction(
            value = ChannelLabelCrossRef::class,
            parentColumn = "channelId",
            entityColumn = "labelId"
        )
    ) val labels: Set<LabelEntity>,
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)