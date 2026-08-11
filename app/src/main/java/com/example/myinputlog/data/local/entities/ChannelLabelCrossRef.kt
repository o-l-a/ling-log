package com.example.myinputlog.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "channel_label_cross_ref",
    primaryKeys = ["channelId", "labelId"],
    indices = [Index("labelId")]
)
data class ChannelLabelCrossRef(
    val channelId: String, val labelId: String
)