package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.ChannelLabelCrossRef
import com.example.myinputlog.data.local.entities.LabelEntity

class ChannelWithLabels(
    @Embedded val channel: ChannelEntity, @Relation(
        parentColumn = "id", entityColumn = "id", associateBy = Junction(
            value = ChannelLabelCrossRef::class,
            parentColumn = "channelId",
            entityColumn = "labelId"
        )
    ) val labels: List<LabelEntity>
)