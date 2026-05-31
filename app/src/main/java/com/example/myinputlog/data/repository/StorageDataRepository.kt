package com.example.myinputlog.data.repository

import androidx.paging.PagingData
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.data.model.UserLabel
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface StorageDataRepository {
    val userCourses: Flow<List<UserCourse>?>
    val currentCourseId: Flow<String>
    val currentUser: Flow<UserData>
    val themeMode: Flow<AppTheme>
    val confettiColors: Flow<ConfettiOptions>
    suspend fun changeUsername(newUsername: String)
    suspend fun signOut()
    suspend fun deleteAccount()
    fun videoPagingFlow(courseId: String): Flow<PagingData<YouTubeVideo>>
    fun channelPagingFlow(courseId: String): Flow<PagingData<YouTubeChannel>>
    suspend fun getYouTubeVideo(courseId: String, videoId: String): YouTubeVideo?
    suspend fun getYouTubeChannel(courseId: String, channelId: String): YouTubeChannel?
    suspend fun getUserCourse(courseId: String): UserCourse?
    suspend fun saveUserCourse(course: UserCourse): String
    suspend fun updateUserCourse(course: UserCourse)
    suspend fun deleteUserCourse(courseId: String)
    fun getMonthlyStatsFlow(userCourseId: String, monthId: String): Flow<UserMonthlyStats?>
    fun getLabelsFlow(userCourseId: String): Flow<List<UserLabel>>
    fun getLabel(courseId: String, labelId: String): Flow<UserLabel?>
    suspend fun saveLabel(courseId: String, label: UserLabel)
    suspend fun deleteLabel(courseId: String, label: UserLabel)

    suspend fun saveVideo(
        courseId: String,
        video: YouTubeVideo,
        originalVideo: YouTubeVideo?,
        channel: YouTubeChannel?
    )

    suspend fun deleteVideo(courseId: String, video: YouTubeVideo)
    suspend fun setCurrentCourse(courseId: String)
    suspend fun saveThemeMode(theme: AppTheme)
    suspend fun saveConfettiColors(colors: ConfettiOptions)
}