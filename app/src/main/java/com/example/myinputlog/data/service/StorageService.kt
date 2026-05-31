package com.example.myinputlog.data.service

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.UserLabel
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow

interface StorageService {
    fun getUserCourses(userId: String): Flow<List<UserCourse>?>
    suspend fun videosByWatchedOnQuery(
        userId: String, courseId: String, lastVideo: DocumentSnapshot?, limitSize: Long
    ): Query

    suspend fun channelsByVideoCount(
        userId: String, courseId: String, lastChannel: DocumentSnapshot?, limitSize: Long
    ): Query

    fun getVideosChangeSignal(userId: String, courseId: String): Flow<Unit>
    fun getLabelsChangeSignal(userId: String, courseId: String): Flow<Timestamp?>
    suspend fun getUserCourse(currentUserId: String, userCourseId: String): UserCourse?
    suspend fun saveUserCourse(currentUserId: String, userCourse: UserCourse): String
    suspend fun updateUserCourse(currentUserId: String, userCourse: UserCourse)
    suspend fun deleteUserCourse(currentUserId: String, userCourseId: String)
    fun getMonthlyStatsFlow(
        currentUserId: String, userCourseId: String, monthId: String
    ): Flow<UserMonthlyStats?>

    suspend fun getLabelsUpdatedAfter(
        currentUserId: String, userCourseId: String, timestamp: Timestamp
    ): List<UserLabel>

    suspend fun getYouTubeVideo(
        currentUserId: String, userCourseId: String, youTubeVideoId: String
    ): YouTubeVideo?

    suspend fun saveYouTubeVideo(
        userId: String,
        courseId: String,
        newVideo: YouTubeVideo,
        oldVideo: YouTubeVideo? = null,
        timestamp: Timestamp = Timestamp.now(),
        channelMetadata: YouTubeChannel? = null,
        channelExistsOnServer: Boolean = false
    )

    suspend fun deleteYouTubeVideo(
        userId: String,
        courseId: String,
        video: YouTubeVideo,
        timestamp: Timestamp = Timestamp.now()
    )

    suspend fun getYouTubeChannel(
        currentUserId: String, userCourseId: String, youTubeChannelId: String
    ): YouTubeChannel?

    suspend fun saveUserLabel(
        userId: String,
        courseId: String,
        label: UserLabel,
        timestamp: Timestamp = Timestamp.now()
    )

    suspend fun deleteUserLabel(
        userId: String,
        courseId: String,
        label: UserLabel,
        timestamp: Timestamp = Timestamp.now()
    )
}