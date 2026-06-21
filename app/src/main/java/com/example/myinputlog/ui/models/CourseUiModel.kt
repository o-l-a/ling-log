package com.example.myinputlog.ui.models

import com.example.myinputlog.data.local.model.CourseWithStats
import java.util.Date

data class CourseUiModel(
    val id: String = "",
    val name: String = "",
    val timestamp: Date = Date(),
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,
    val countryGroupId: String = "",
    val totalTimeInSeconds: Long = 0L,
    val videoCount: Long = 0L,
    val totalActiveDays: Long = 0L,
)

fun CourseWithStats.toCourseUiModel(): CourseUiModel = CourseUiModel(
    id = course.id,
    name = course.name,
    timestamp = course.timestamp,
    goalInHours = course.goalInHours,
    otherSourceHours = course.otherSourceHours,
    countryGroupId = course.countryGroupId,
    totalTimeInSeconds = totalTimeInSeconds,
    videoCount = totalVideoCount,
    totalActiveDays = totalActiveDays
)