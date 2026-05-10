package com.example.myinputlog.data.service

import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface StorageService {
    fun getUserCourses(userId: String): Flow<List<UserCourse>?>
    suspend fun videosByWatchedOnQuery(
        userId: String, courseId: String, lastVideo: DocumentSnapshot?, limitSize: Long
    ): Query

    suspend fun getUserCourse(currentUserId: String, userCourseId: String): UserCourse?
    suspend fun saveUserCourse(currentUserId: String, userCourse: UserCourse): String
    suspend fun updateUserCourse(currentUserId: String, userCourse: UserCourse)
    suspend fun deleteUserCourse(currentUserId: String, userCourseId: String)
    suspend fun getCourseStatistics(currentUserId: String, userCourseId: String): CourseStatistics
    suspend fun getMonthlyAggregateData(currentUserId: String, userCourseId: String, yearMonth: YearMonth): List<Long>

    suspend fun getYouTubeVideo(currentUserId: String, userCourseId: String, youTubeVideoId: String): YouTubeVideo?
    suspend fun saveYouTubeVideo(currentUserId: String, userCourseId: String, youTubeVideo: YouTubeVideo)
    suspend fun updateYouTubeVideo(currentUserId: String, userCourseId: String, youTubeVideo: YouTubeVideo)
    suspend fun deleteYouTubeVideo(currentUserId: String, userCourseId: String, youTubeVideoId: String)
}