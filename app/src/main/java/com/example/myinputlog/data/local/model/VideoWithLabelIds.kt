package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.entities.VideoLabelCrossRef

data class VideoWithLabelIds(
    @Embedded val video: VideoEntity, @Relation(
        parentColumn = "id", entityColumn = "labelId", associateBy = Junction(
            value = VideoLabelCrossRef::class, parentColumn = "videoId", entityColumn = "labelId"
        ), projection = ["labelId"]
    ) val labelIds: List<String>
)