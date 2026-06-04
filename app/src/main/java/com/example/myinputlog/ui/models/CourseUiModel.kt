package com.example.myinputlog.ui.models

import com.example.myinputlog.data.local.entities.CourseEntity

data class CourseUiModel(
    val id: String = "",
    val name: String = "",
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,
)

fun CourseEntity.toCourseUiModel(): CourseUiModel = CourseUiModel(
    id, name, goalInHours, otherSourceHours
)