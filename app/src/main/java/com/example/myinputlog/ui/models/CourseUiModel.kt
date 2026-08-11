package com.example.myinputlog.ui.models

import com.example.myinputlog.data.local.model.CourseWithStats
import com.example.myinputlog.data.utils.StringProvider
import java.util.Date

data class CourseUiModel(
    val id: String = "",
    val name: String = "",
    val timestamp: Date = Date(),
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,
    val countryGroup: CountryGroupUiModel = CountryGroupUiModel(),
    val totalTimeInSeconds: Long = 0L,
    val videoCount: Long = 0L,
    val totalActiveDays: Long = 0L,
)

fun CourseWithStats.toCourseUiModel(stringProvider: StringProvider): CourseUiModel = CourseUiModel(
    id = course.id,
    name = course.name,
    timestamp = course.timestamp,
    goalInHours = course.goalInHours,
    otherSourceHours = course.otherSourceHours,
    countryGroup = countryGroup?.toUiModel(stringProvider) ?: CountryGroupUiModel(),
    totalTimeInSeconds = totalTimeInSeconds,
    videoCount = totalVideoCount,
    totalActiveDays = totalActiveDays
)