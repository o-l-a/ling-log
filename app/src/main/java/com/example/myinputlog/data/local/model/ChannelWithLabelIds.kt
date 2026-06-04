package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.ChannelLabelCrossRef

data class ChannelWithLabelIds(
    @Embedded val channel: ChannelEntity, @Relation(
        parentColumn = "id",
        entityColumn = "channelId",
        entity = ChannelLabelCrossRef::class,
        projection = ["labelId"]
    ) val labelIds: List<String>
)