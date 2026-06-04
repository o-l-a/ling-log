package com.example.myinputlog.ui.models

import com.example.myinputlog.data.local.entities.CourseEntity
import java.util.Date

data class CourseUiModel(
    val id: String = "",
    val name: String = "",
    val timestamp: Date = Date(),
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,
)

fun CourseEntity.toCourseUiModel(): CourseUiModel = CourseUiModel(
    id, name, timestamp, goalInHours, otherSourceHours
)