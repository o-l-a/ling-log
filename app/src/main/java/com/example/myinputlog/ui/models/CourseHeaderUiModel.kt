package com.example.myinputlog.ui.models

import com.example.myinputlog.data.model.UserCourse
import java.util.concurrent.TimeUnit

data class CourseHeaderUiModel(
    val id: String = "",
    val name: String = "",
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,
    val progress: Float = 0F,
    val totalTimeInSeconds: Long = 0L,
    val totalTimeInSecondsToday: Long = 0L,
    val videoCount: Long = 0L,
    val percentageText: String = ""
)

fun mapToCourseUiModel(
    course: UserCourse, totalTimeInSecondsToday: Long = 0L
): CourseHeaderUiModel {
    val totalTimeInSeconds = course.totalTimeInSeconds + TimeUnit.HOURS.toSeconds(course.otherSourceHours)

    val progressValue = if (course.goalInHours != 0L) {
        TimeUnit.SECONDS.toHours(totalTimeInSeconds).toFloat() / course.goalInHours.toFloat()
    } else {
        0F
    }

    return CourseHeaderUiModel(
        id = course.id,
        name = course.name,
        goalInHours = course.goalInHours,
        otherSourceHours = course.otherSourceHours,
        progress = progressValue,
        totalTimeInSeconds = totalTimeInSeconds,
        totalTimeInSecondsToday = totalTimeInSecondsToday,
        videoCount = course.totalVideoCount,
        percentageText = "${(progressValue * 100).toInt()}%"
    )
}