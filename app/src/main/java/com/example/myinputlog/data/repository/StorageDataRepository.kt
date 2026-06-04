package com.example.myinputlog.data.repository

import androidx.paging.PagingData
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.model.ChannelWithLabels
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
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
    val courses: Flow<List<CourseEntity>>
    val currentCourseId: Flow<String>
    val currentUser: Flow<UserData>
    val themeMode: Flow<AppTheme>
    val confettiColors: Flow<ConfettiOptions>
    suspend fun changeUsername(newUsername: String)
    suspend fun signOut()
    suspend fun deleteAccount()
    fun videoPagingFlow(courseId: String): Flow<PagingData<VideoWithChannelAndLabels>>
    fun channelPagingFlow(courseId: String): Flow<PagingData<ChannelWithLabels>>
    suspend fun getVideo(videoId: String): VideoWithChannelAndLabels?
    suspend fun getChannel(channelId: String): ChannelWithLabels?
    suspend fun getUserCourse(courseId: String): CourseEntity?
    suspend fun saveUserCourse(course: UserCourse): String
    suspend fun updateUserCourse(course: UserCourse)
    suspend fun deleteUserCourse(courseId: String)
    fun getMonthlyStatsFlow(userCourseId: String, monthId: String): Flow<UserMonthlyStats?>
    fun getLabelsFlow(): Flow<List<LabelEntity>>
    suspend fun getLabelById(labelId: String): LabelEntity?
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