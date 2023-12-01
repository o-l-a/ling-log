package com.example.myinputlog.data.service

import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface StorageService {
    val userCourses: Flow<List<UserCourse>>
    suspend fun videosByWatchedOnQuery(courseId: String, lastVideoId: String?, limitSize: Long): Query

    suspend fun getUserCourse(userCourseId: String): UserCourse?
    suspend fun saveUserCourse(userCourse: UserCourse)
    suspend fun updateUserCourse(userCourse: UserCourse)
    suspend fun deleteUserCourse(userCourseId: String)
    suspend fun getCourseStatistics(userCourseId: String): CourseStatistics
    suspend fun getMonthlyAggregateData(userCourseId: String, yearMonth: YearMonth): List<Long>

    suspend fun getYouTubeVideo(userCourseId: String, youTubeVideoId: String): YouTubeVideo?
    suspend fun saveYouTubeVideo(userCourseId: String, youTubeVideo: YouTubeVideo)
    suspend fun updateYouTubeVideo(userCourseId: String, youTubeVideo: YouTubeVideo)
    suspend fun deleteYouTubeVideo(userCourseId: String, youTubeVideoId: String)

    suspend fun deleteAllForUser(userId: String)
    suspend fun deleteAllVideosForCourse(userId: String, userCourseId: String)
}