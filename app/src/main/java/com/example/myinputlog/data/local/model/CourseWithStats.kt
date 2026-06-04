package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import com.example.myinputlog.data.local.entities.CourseEntity

data class CourseWithStats(
    @Embedded val course: CourseEntity,
    val totalActiveDays: Long = 0L,
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)