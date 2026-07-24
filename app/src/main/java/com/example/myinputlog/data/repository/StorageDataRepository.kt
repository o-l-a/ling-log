package com.example.myinputlog.data.repository

import androidx.paging.PagingData
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.CountryGroupEntity
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels
import com.example.myinputlog.data.local.model.CourseWithStats
import com.example.myinputlog.data.local.model.DailyWatchStat
import com.example.myinputlog.data.local.model.DailyWatchWrapper
import com.example.myinputlog.data.local.model.LabelWithStats
import com.example.myinputlog.data.local.model.RegionStat
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.MonthlyStatsUiModel
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.screens.common.ConfettiOptions
import com.example.myinputlog.ui.screens.media_list.MediaFilters
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface StorageDataRepository {
    val courses: Flow<List<CourseWithStats>>
    val labels: Flow<Set<LabelEntity>>
    val countryGroups: Flow<List<CountryGroupEntity>>
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
    fun videoPagingFlow(
        courseId: String, filters: MediaFilters, sort: SortOptions
    ): Flow<PagingData<VideoUiModel>>

    suspend fun getVideo(videoId: String): VideoWithChannelAndLabels?
    suspend fun saveVideo(
        video: VideoEntity,
        channel: ChannelEntity,
        labelIds: List<String>,
        syncLabelsToChannel: Boolean
    )

    suspend fun deleteVideo(videoId: String)

    // channel
    fun channelPagingFlow(
        courseId: String,
        filters: MediaFilters,
        sort: SortOptions,
        podium: Map<String, Int> = emptyMap()
    ): Flow<PagingData<ChannelUiModel>>

    suspend fun getChannel(channelId: String): ChannelWithStatsAndLabels?
    suspend fun getChannelGlobalRanking(): Map<String, Int>
    suspend fun getChannelIdsForCourse(courseId: String): Set<String>
    suspend fun saveChannel(
        channel: ChannelEntity,
        labelIds: List<String>,
        initialLabelIds: List<String>,
        syncLabelsToVideos: Boolean
    )

    suspend fun deleteChannel(channelId: String)

    // course
    suspend fun getUserCourse(courseId: String): CourseWithStats?
    suspend fun saveUserCourse(course: CourseEntity)
    suspend fun deleteUserCourse(courseId: String)

    // label
    suspend fun getLabelById(labelId: String): LabelEntity?
    suspend fun saveLabel(label: LabelEntity)
    suspend fun deleteLabel(labelId: String)
    suspend fun getAllLabelsAsSet(): Set<LabelEntity>

    // app config
    suspend fun getCountryGroupById(countryGroupId: String): CountryGroupEntity?
    suspend fun getCountriesForCourse(courseId: String): List<String>
    fun getCountriesFlow(courseId: String): Flow<List<String>>

    // stats
    fun getMonthlyStatsFlow(courseId: String, monthId: String): Flow<MonthlyStatsUiModel?>
    fun getTodaySecondsFlow(courseId: String): Flow<Long>
    fun getDailyWatchStats(courseId: String, start: Long, end: Long): Flow<DailyWatchWrapper>
    fun getBaselineProgress(courseId: String, start: Long): Flow<Long>
    fun getRegionStats(courseId: String, start: Long, end: Long): Flow<List<RegionStat>>
    fun getLabelStats(courseId: String, start: Long, end: Long): Flow<List<LabelWithStats>>
    fun getTopChannelsWithStatsAndLabels(
        courseId: String, start: Long, end: Long, limit: Int = 5
    ): Flow<List<ChannelWithStatsAndLabels>>

    // preferences
    suspend fun setCurrentCourse(courseId: String)
    suspend fun saveThemeMode(theme: AppTheme)
    suspend fun saveConfettiColors(colors: ConfettiOptions)
}