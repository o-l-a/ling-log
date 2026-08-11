package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.entities.VideoLabelCrossRef

data class VideoWithLabelIds(
    @Embedded val video: VideoEntity, @Relation(
        parentColumn = "id",
        entityColumn = "videoId",
        entity = VideoLabelCrossRef::class,
        projection = ["labelId"]
    ) val labelIds: List<String>
)