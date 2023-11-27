package com.example.myinputlog.ui.screens.video_list

import androidx.paging.PagingData
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

data class VideoListUiState(
    val currentCourseId: String = "",
    val currentCourseName: String = "",
    val currentCourseGoalInHours: String = "",
    val currentCourseOtherSourceHours: String = "",
    val courseStatistics: CourseStatistics = CourseStatistics(),

    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val videos: Flow<PagingData<YouTubeVideo>> = emptyFlow(),
    val isLoading: Boolean = true
)

fun UserCourse.toVideoListUiState(
    isLoading: Boolean = true,
    courseStatistics: CourseStatistics = CourseStatistics(),
): VideoListUiState = VideoListUiState(
    currentCourseId = id,
    currentCourseName = name,
    currentCourseGoalInHours = goalInHours.toString(),
    currentCourseOtherSourceHours = otherSourceHours.toString(),
    courseStatistics = courseStatistics,
    isLoading = isLoading,
)

fun VideoListUiState.toUserCourse(): UserCourse = UserCourse(
    id = currentCourseId,
    name = currentCourseName,
    goalInHours = currentCourseGoalInHours.toLongOrNull() ?: 0L,
    otherSourceHours = currentCourseOtherSourceHours.toLongOrNull() ?: 0L
)