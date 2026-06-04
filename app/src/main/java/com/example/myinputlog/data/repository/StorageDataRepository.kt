package com.example.myinputlog.data.repository

import androidx.paging.PagingData
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.model.ChannelWithLabels
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.data.model.UserLabel
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface StorageDataRepository {
    val courses: Flow<List<CourseEntity>>
    val currentCourseId: Flow<String>
    val currentUser: Flow<UserData>
    val themeMode: Flow<AppTheme>
    val confettiColors: Flow<ConfettiOptions>

    // account
    suspend fun changeUsername(newUsername: String)
    suspend fun signOut()
    suspend fun deleteAccount()

    // video
    fun videoPagingFlow(courseId: String): Flow<PagingData<VideoWithChannelAndLabels>>
    suspend fun getVideo(videoId: String): VideoWithChannelAndLabels?
    suspend fun saveVideo(
        video: VideoEntity,
        channel: ChannelEntity,
        labelIds: List<String>,
        syncLabelsToChannel: Boolean
    )

    suspend fun deleteVideo(videoId: String)

    // channel
    fun channelPagingFlow(courseId: String): Flow<PagingData<ChannelWithLabels>>
    suspend fun getChannel(channelId: String): ChannelWithLabels?

    // course
    suspend fun getUserCourse(courseId: String): CourseEntity?
    suspend fun saveUserCourse(course: CourseUiModel): String
    suspend fun updateUserCourse(course: CourseUiModel)
    suspend fun deleteUserCourse(courseId: String)

    // label
    fun getLabelsFlow(): Flow<List<LabelEntity>>
    suspend fun getLabelById(labelId: String): LabelEntity?
    suspend fun saveLabel(courseId: String, label: UserLabel)
    suspend fun deleteLabel(labelId: String)

    // stats
    fun getMonthlyStatsFlow(userCourseId: String, monthId: String): Flow<UserMonthlyStats?>

    // preferences
    suspend fun setCurrentCourse(courseId: String)
    suspend fun saveThemeMode(theme: AppTheme)
    suspend fun saveConfettiColors(colors: ConfettiOptions)
}