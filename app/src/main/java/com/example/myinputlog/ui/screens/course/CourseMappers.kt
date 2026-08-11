package com.example.myinputlog.ui.screens.course

import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.ui.models.CourseUiModel
import java.util.UUID

fun CourseUiModel.toCourseEntity(): CourseEntity = CourseEntity(
    id = id.ifBlank { UUID.randomUUID().toString() },
    name = name,
    goalInHours = goalInHours,
    otherSourceHours = otherSourceHours,
    countryGroupId = countryGroup.id,
    timestamp = timestamp,
    lastUpdated = System.currentTimeMillis(),
)