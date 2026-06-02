package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.entities.VideoLabelCrossRef

data class VideoWithLabels(
    @Embedded val video: VideoEntity, @Relation(
        parentColumn = "id", entityColumn = "id", associateBy = Junction(
            value = VideoLabelCrossRef::class, parentColumn = "videoId", entityColumn = "labelId"
        )
    ) val labels: List<LabelEntity>
)