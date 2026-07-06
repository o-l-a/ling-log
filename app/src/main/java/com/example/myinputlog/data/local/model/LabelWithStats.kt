package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import com.example.myinputlog.data.local.entities.LabelEntity

data class LabelWithStats(
    @Embedded val label: LabelEntity,
    val totalTimeInSeconds: Long = 0L
)