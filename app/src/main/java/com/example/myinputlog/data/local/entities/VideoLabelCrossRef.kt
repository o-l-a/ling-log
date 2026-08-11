package com.example.myinputlog.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "video_label_cross_ref",
    primaryKeys = ["videoId", "labelId"],
    indices = [Index("labelId")]
)
data class VideoLabelCrossRef(
    val videoId: String, val labelId: String
)