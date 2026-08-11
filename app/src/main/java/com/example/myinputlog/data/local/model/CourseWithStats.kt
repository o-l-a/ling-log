package com.example.myinputlog.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myinputlog.data.local.entities.CountryGroupEntity
import com.example.myinputlog.data.local.entities.CourseEntity

data class CourseWithStats(
    @Embedded val course: CourseEntity,
    @Relation(
        parentColumn = "countryGroupId", entityColumn = "id"
    ) val countryGroup: CountryGroupEntity?,
    val totalActiveDays: Long = 0L,
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)