package com.example.myinputlog.data.service

import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import kotlinx.coroutines.flow.Flow

interface StorageService {
    val userCourses: Flow<List<UserCourse>>
    fun videos(courseId: String): Flow<List<YouTubeVideo>>

    suspend fun getUserCourse(userCourseId: String): UserCourse?
    suspend fun saveUserCourse(userCourse: UserCourse)
    suspend fun updateUserCourse(userCourse: UserCourse)
    suspend fun deleteUserCourse(userCourseId: String)
    suspend fun getCourseStatistics(userCourseId: String): CourseStatistics

    suspend fun getYouTubeVideo(userCourseId: String, youTubeVideoId: String): YouTubeVideo?
    suspend fun saveYouTubeVideo(userCourseId: String, youTubeVideo: YouTubeVideo)
    suspend fun updateYouTubeVideo(userCourseId: String, youTubeVideo: YouTubeVideo)
    suspend fun deleteYouTubeVideo(userCourseId: String, youTubeVideoId: String)

    suspend fun deleteAllForUser(userId: String)
    suspend fun deleteAllVideosForCourse(userCourseId: String)
}