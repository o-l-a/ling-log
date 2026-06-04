package com.example.myinputlog.ui.models

import com.example.myinputlog.ui.models.CourseUiModel
import java.util.concurrent.TimeUnit

data class CourseHeaderUiModel(
    val id: String = "",
    val name: String = "",
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,
    val progress: Float = 0F,
    val totalTimeInSeconds: Long = 0L,
    val totalTimeInSecondsToday: Long = 0L,
    val dailyAverageSeconds: Long = 0L,
    val totalActiveDays: String = "",
    val videoCount: Long = 0L,
    val percentageText: String = ""
)

fun mapToCourseUiModel(
    course: CourseUiModel, totalTimeInSecondsToday: Long = 0L
): CourseHeaderUiModel {
    val totalTimeInSeconds =
        course.totalTimeInSeconds + TimeUnit.HOURS.toSeconds(course.otherSourceHours)

    val progressValue = if (course.goalInHours != 0L) {
        TimeUnit.SECONDS.toHours(totalTimeInSeconds).toFloat() / course.goalInHours.toFloat()
    } else {
        0F
    }

    val dailyAverageSeconds =
        if (course.totalActiveDays == 0L) 0L else course.totalTimeInSeconds / course.totalActiveDays

    return CourseHeaderUiModel(
        id = course.id,
        name = course.name,
        goalInHours = course.goalInHours,
        otherSourceHours = course.otherSourceHours,
        progress = progressValue,
        totalTimeInSeconds = totalTimeInSeconds,
        totalTimeInSecondsToday = totalTimeInSecondsToday,
        dailyAverageSeconds = dailyAverageSeconds,
        totalActiveDays = course.totalActiveDays.toString(),
        videoCount = course.totalVideoCount,
        percentageText = "${(progressValue * 100).toInt()}%"
    )
}