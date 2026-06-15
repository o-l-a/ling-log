package com.example.myinputlog.data.repository

import androidx.paging.PagingData
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels
import com.example.myinputlog.data.local.model.CourseWithStats
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.MonthlyStatsUiModel
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.screens.common.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface StorageDataRepository {
    val courses: Flow<List<CourseWithStats>>
    val labels: Flow<Set<LabelEntity>>
    val currentCourseId: Flow<String>
    val currentUser: Flow<UserData>
    val themeMode: Flow<AppTheme>
    val confettiColors: Flow<ConfettiOptions>

    // account
    suspend fun changeUsername(newUsername: String)
    suspend fun signOut()
    suspend fun createAccount(email: String, password: String, username: String)
    suspend fun deleteAccount()

    // video
    fun videoPagingFlow(courseId: String): Flow<PagingData<VideoUiModel>>
    suspend fun getVideo(videoId: String): VideoWithChannelAndLabels?
    suspend fun saveVideo(
        video: VideoEntity,
        channel: ChannelEntity,
        labelIds: List<String>,
        syncLabelsToChannel: Boolean
    )

    suspend fun deleteVideo(videoId: String)

    // channel
    fun channelPagingFlow(courseId: String): Flow<PagingData<ChannelUiModel>>
    suspend fun getChannel(channelId: String): ChannelWithStatsAndLabels?
    suspend fun saveChannel(
        channel: ChannelEntity,
        labelIds: List<String>,
        initialLabelIds: List<String>,
        syncLabelsToVideos: Boolean
    )
    suspend fun deleteChannel(channelId: String)

    // course
    suspend fun getUserCourse(courseId: String): CourseEntity?
    suspend fun saveUserCourse(course: CourseEntity)
    suspend fun deleteUserCourse(courseId: String)

    // label
    suspend fun getLabelById(labelId: String): LabelEntity?
    suspend fun saveLabel(label: LabelEntity)
    suspend fun deleteLabel(labelId: String)
    suspend fun getAllLabelsAsSet(): Set<LabelEntity>

    // stats
    fun getMonthlyStatsFlow(courseId: String, monthId: String): Flow<MonthlyStatsUiModel?>
    fun getTodaySecondsFlow(courseId: String): Flow<Long>

    // preferences
    suspend fun setCurrentCourse(courseId: String)
    suspend fun saveThemeMode(theme: AppTheme)
    suspend fun saveConfettiColors(colors: ConfettiOptions)
}