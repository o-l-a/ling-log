package com.example.myinputlog.data.repository.impl

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.example.myinputlog.data.local.AppDatabase
import com.example.myinputlog.data.local.dao.ChannelDao
import com.example.myinputlog.data.local.dao.CourseDao
import com.example.myinputlog.data.local.dao.LabelDao
import com.example.myinputlog.data.local.dao.StatsDao
import com.example.myinputlog.data.local.dao.VideoDao
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.model.ChannelWithLabelIds
import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels
import com.example.myinputlog.data.local.model.CourseWithStats
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import com.example.myinputlog.data.local.model.VideoWithLabelIds
import com.example.myinputlog.data.local.query.ChannelQueryBuilder
import com.example.myinputlog.data.local.query.VideoQueryBuilder
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.data.service.StorageService
import com.example.myinputlog.data.utils.DateUtils.toMonthKey
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.DayAggregation
import com.example.myinputlog.ui.models.MonthlyStatsUiModel
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.models.toChannelUiModel
import com.example.myinputlog.ui.models.toVideoUiModel
import com.example.myinputlog.ui.screens.common.ConfettiOptions
import com.example.myinputlog.ui.screens.media_list.MediaFilters
import com.example.myinputlog.ui.theme.AppTheme
import com.example.myinputlog.worker.PushSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

class DefaultStorageDataRepository @Inject constructor(
    private val accountService: AccountService,
    private val preferenceStorageService: PreferenceStorageService,
    private val storageService: StorageService,
    private val pagingConfig: PagingConfig,
    private val workManager: WorkManager,
    private val db: AppDatabase,
    private val courseDao: CourseDao,
    private val labelDao: LabelDao,
    private val videoDao: VideoDao,
    private val channelDao: ChannelDao,
    private val statsDao: StatsDao
) : StorageDataRepository {

    override val courses: Flow<List<CourseWithStats>> = courseDao.getAllCourses()

    override val labels: Flow<Set<LabelEntity>> =
        labelDao.getAllLabels().map { list -> list.toSet() }

    override val currentUser: Flow<UserData> = accountService.currentUser

    @OptIn(ExperimentalCoroutinesApi::class)
    override val themeMode: Flow<AppTheme> =
        currentUser.distinctUntilChanged().flatMapLatest { user ->
            preferenceStorageService.themeMode(user.id)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val confettiColors: Flow<ConfettiOptions> =
        currentUser.distinctUntilChanged().flatMapLatest { user ->
            preferenceStorageService.confettiColors(user.id)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentCourseId: Flow<String> =
        currentUser.distinctUntilChanged().flatMapLatest { user ->
            preferenceStorageService.currentCourseId(user.id)
        }


    // account
    override suspend fun changeUsername(newUsername: String) = withContext(Dispatchers.IO) {
        accountService.changeUsername(newUsername)
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        accountService.signOut()
    }

    override suspend fun createAccount(email: String, password: String, username: String) {
        val uid = accountService.createAccount(email, password, username)
        uid?.let { storageService.initializeUser(uid) }
    }

    override suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        val uid = accountService.currentUserId

        val courseIds = courseDao.getAllIds()
        val channelIds = channelDao.getAllIds()
        val monthKeys = videoDao.getAllUniqueMonthKeys().map { it.toMonthKey() }

        storageService.deleteAllForUser(uid, courseIds, channelIds, monthKeys)
        db.clearAllTables()
        accountService.deleteAccount()
    }

    override fun videoPagingFlow(
        courseId: String, filters: MediaFilters
    ): Flow<PagingData<VideoUiModel>> {
        return Pager(
            config = pagingConfig, pagingSourceFactory = {
                val query = VideoQueryBuilder.build(courseId, filters)
                videoDao.getVideosPagingSource(query)
            }).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toVideoUiModel()
            }
        }
    }

    override suspend fun getVideo(videoId: String): VideoWithChannelAndLabels? =
        withContext(Dispatchers.IO) {
            return@withContext videoDao.getVideoWithChannelAndLabelsById(videoId)
        }

    override suspend fun saveVideo(
        video: VideoEntity,
        channel: ChannelEntity,
        labelIds: List<String>,
        syncLabelsToChannel: Boolean
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            channelDao.upsertChannel(channel)
            videoDao.upsertVideoWithLabelIds(VideoWithLabelIds(video, labelIds))
            if (syncLabelsToChannel) {
                channelDao.upsertChannelWithLabelIds(ChannelWithLabelIds(channel, labelIds))
            } else {
                channelDao.upsertChannel(channel)
            }
        }

        schedulePushSync()
    }

    override suspend fun deleteVideo(videoId: String) = withContext(Dispatchers.IO) {
        db.withTransaction {
            videoDao.deleteVideoById(videoId)
            videoDao.deleteLabelRefsForVideo(videoId)
        }
        schedulePushSync()
    }

    // channel
    override fun channelPagingFlow(
        courseId: String, filters: MediaFilters, podium: Map<String, Int>
    ): Flow<PagingData<ChannelUiModel>> {
        return Pager(
            config = pagingConfig, pagingSourceFactory = {
                val query = ChannelQueryBuilder.build(courseId, filters)
                channelDao.getChannelsPagingSource(query)
            }).flow.map { pagingData ->
            pagingData.map { entity ->
                val rank = podium.getOrDefault(entity.channel.id, 0)
                entity.toChannelUiModel(rank)
            }
        }
    }

    override suspend fun getChannel(channelId: String): ChannelWithStatsAndLabels? =
        withContext(Dispatchers.IO) {
            return@withContext channelDao.getChannelWithLabelsById(channelId)
        }

    override suspend fun getChannelGlobalRanking(): Map<String, Int> {
        return channelDao.getGlobalChannelRanking(limit = 3)
            .mapIndexed { index, id -> id to (index + 1) }.toMap()
    }

    override suspend fun saveChannel(
        channel: ChannelEntity,
        labelIds: List<String>,
        initialLabelIds: List<String>,
        syncLabelsToVideos: Boolean
    ) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Labels will ${if (!syncLabelsToVideos) "not " else ""}be synced.")
        Log.d(TAG, "Channel id: ${channel.id}, channel name: ${channel.title}")
        val addedLabels = labelIds - initialLabelIds.toSet()
        val removedLabels = initialLabelIds - labelIds.toSet()
        Log.d(TAG, "Labels to add: $addedLabels, labels to remove: $removedLabels")
        db.withTransaction {
            channelDao.upsertChannelWithLabelIds(ChannelWithLabelIds(channel, labelIds))
            if (syncLabelsToVideos && (addedLabels.isNotEmpty() || removedLabels.isNotEmpty())) {
                videoDao.syncLabelsToChannel(channel.id, addedLabels, removedLabels)
            }
        }
        schedulePushSync()
    }

    override suspend fun deleteChannel(channelId: String) = withContext(Dispatchers.IO) {
        db.withTransaction {
            channelDao.deleteChannelById(channelId)
            channelDao.deleteLabelRefsForChannel(channelId)
        }
        schedulePushSync()
    }

    // course
    override suspend fun getUserCourse(courseId: String): CourseEntity? =
        withContext(Dispatchers.IO) {
            return@withContext courseDao.getCourseById(courseId)
        }

    override suspend fun saveUserCourse(course: CourseEntity) = withContext(Dispatchers.IO) {
        courseDao.upsertCourse(course)
        setCurrentCourse(course.id)
        schedulePushSync()
    }

    override suspend fun deleteUserCourse(courseId: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            courseDao.deleteCourseById(courseId, now)
            courseDao.bulkDeleteChannelsForCourse(courseId, now)
            courseDao.bulkDeleteVideosForCourse(courseId, now)
        }
        val currentCourseId = currentCourseId.firstOrNull() ?: ""
        if (currentCourseId == courseId) {
            val uid = accountService.currentUserId
            val firstAvailable = courses.firstOrNull()?.getOrNull(0)
            if (firstAvailable == null) {
                preferenceStorageService.clearCurrentCourseId(uid)
            } else {
                preferenceStorageService.saveCurrentCourseId(uid, courseId)
            }
        }
        schedulePushSync()
    }

    // label
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getLabelById(labelId: String): LabelEntity? = withContext(Dispatchers.IO) {
        return@withContext labelDao.getLabelById(labelId)
    }

    override suspend fun getAllLabelsAsSet(): Set<LabelEntity> = withContext(Dispatchers.IO) {
        return@withContext labelDao.getAllLabelsAsList().toSet()
    }

    override suspend fun saveLabel(label: LabelEntity) = withContext(Dispatchers.IO) {
        labelDao.upsertLabel(label)
        schedulePushSync()
    }

    override suspend fun deleteLabel(labelId: String) = withContext(Dispatchers.IO) {
        labelDao.deleteLabelById(labelId)
        schedulePushSync()
    }

    // stats
    override fun getMonthlyStatsFlow(
        courseId: String, monthId: String
    ): Flow<MonthlyStatsUiModel?> {
        val yearMonth = YearMonth.parse(monthId)
        val start =
            yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end =
            yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()

        return statsDao.getDailyStats(start, end).map { rows ->
            val daysMap = rows.associate {
                "day_${it.dayOfMonth.toInt()}" to DayAggregation(
                    totalTimeInSeconds = it.totalSeconds, totalVideoCount = it.videoCount
                )
            }

            MonthlyStatsUiModel(
                id = monthId,
                totalTimeInSeconds = rows.sumOf { it.totalSeconds },
                totalVideoCount = rows.sumOf { it.videoCount },
                days = daysMap
            )
        }
    }

    override fun getTodaySecondsFlow(courseId: String): Flow<Long> {
        val today = LocalDate.now()
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end =
            today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return statsDao.getDailyStats(start, end).map { rows ->
            rows.sumOf { it.totalSeconds }
        }
    }

    // preferences
    override suspend fun saveThemeMode(theme: AppTheme) = withContext(Dispatchers.IO) {
        val uid = accountService.currentUserId
        preferenceStorageService.saveThemeMode(uid, theme)
    }

    override suspend fun saveConfettiColors(colors: ConfettiOptions) = withContext(Dispatchers.IO) {
        val uid = accountService.currentUserId
        preferenceStorageService.saveConfettiColors(uid, colors)
    }

    override suspend fun setCurrentCourse(courseId: String) = withContext(Dispatchers.IO) {
        val uid = accountService.currentUserId
        preferenceStorageService.saveCurrentCourseId(uid, courseId)
    }

    // sync
    private fun schedulePushSync() {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val pushSync = OneTimeWorkRequestBuilder<PushSyncWorker>().setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("pushSync")
            .build()

        workManager.beginUniqueWork(
            "immediate_push_sync", ExistingWorkPolicy.APPEND_OR_REPLACE, listOf(pushSync)
        ).enqueue()
    }

    companion object {
        private const val TAG = "StorageDataRepository"
    }
}