package com.example.myinputlog.ui.models

import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import java.util.concurrent.TimeUnit

data class CourseHeaderUiModel(
    val id: String = "",
    val name: String = "",
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,
    val progress: Float = 0F,
    val totalHours: Long = 0L,
    val percentageText: String = ""
)

fun mapToCourseUiModel(
    course: UserCourse, courseStatistics: CourseStatistics
): CourseHeaderUiModel {
    val hoursWatched = TimeUnit.SECONDS.toHours(
        courseStatistics.timeWatched
    )
    val totalHours = hoursWatched + course.otherSourceHours
    val progressValue = if (course.goalInHours != 0L) {
        totalHours.toFloat() / course.goalInHours.toFloat()
    } else {
        0F
    }

    return CourseHeaderUiModel(
        id = course.id,
        name = course.name,
        goalInHours = course.goalInHours,
        otherSourceHours = course.otherSourceHours,
        progress = progressValue,
        totalHours = totalHours,
        percentageText = "${(progressValue * 100).toInt()}%"
    )
}