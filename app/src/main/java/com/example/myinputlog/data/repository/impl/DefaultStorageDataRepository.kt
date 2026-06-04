package com.example.myinputlog.data.repository.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
import com.example.myinputlog.data.local.dao.VideoDao
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.model.ChannelWithLabelIds
import com.example.myinputlog.data.local.model.ChannelWithLabels
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import com.example.myinputlog.data.local.model.VideoWithLabelIds
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.data.model.UserLabel
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.data.worker.PushSyncWorker
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class DefaultStorageDataRepository @Inject constructor(
    private val accountService: AccountService,
    private val preferenceStorageService: PreferenceStorageService,
    private val pagingConfig: PagingConfig,
    private val workManager: WorkManager,
    private val db: AppDatabase,
    private val courseDao: CourseDao,
    private val labelDao: LabelDao,
    private val videoDao: VideoDao,
    private val channelDao: ChannelDao
) : StorageDataRepository {
    companion object {
        private const val TAG = "StorageRepository"
    }

    override val courses: Flow<List<CourseEntity>> = courseDao.getAllCourses()

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
    override suspend fun changeUsername(newUsername: String) {
        accountService.changeUsername(newUsername)
    }

    override suspend fun signOut() {
        accountService.signOut()
    }

    override suspend fun deleteAccount() {
        accountService.deleteAccount()
    }

    // video
    override fun videoPagingFlow(courseId: String): Flow<PagingData<VideoWithChannelAndLabels>> {
        return Pager(
            config = pagingConfig, pagingSourceFactory = {
                videoDao.getVideosPagingSource(courseId)
            }).flow
    }

    override suspend fun getVideo(videoId: String): VideoWithChannelAndLabels? {
        return videoDao.getVideoWithChannelAndLabelsById(videoId)
    }

    override suspend fun saveVideo(
        video: VideoEntity,
        channel: ChannelEntity,
        labelIds: List<String>,
        syncLabelsToChannel: Boolean
    ) {
        db.withTransaction {
            videoDao.upsertVideoWithLabelIds(VideoWithLabelIds(video, labelIds))
            channelDao.upsertChannel(channel)
            if (syncLabelsToChannel) {
                channelDao.upsertChannelWithLabelIds(ChannelWithLabelIds(channel, labelIds))
            } else {
                channelDao.upsertChannel(channel)
            }
        }

        schedulePushSync()
    }

    override suspend fun deleteVideo(videoId: String) {
        db.withTransaction {
            videoDao.deleteVideoById(videoId)
            videoDao.deleteLabelRefsForVideo(videoId)
        }
        schedulePushSync()
    }

    // channel
    override fun channelPagingFlow(courseId: String): Flow<PagingData<ChannelWithLabels>> {
        return Pager(
            config = pagingConfig, pagingSourceFactory = {
                channelDao.getChannelsPagingSource(courseId)
            }).flow
    }

    override suspend fun getChannel(channelId: String): ChannelWithLabels? {
        return channelDao.getChannelWithLabelsById(channelId)
    }

    // course
    override suspend fun getUserCourse(courseId: String): CourseEntity? {
        return courseDao.getCourseById(courseId)
    }

    override suspend fun saveUserCourse(course: CourseUiModel): String {
        val uid = accountService.currentUserId
        return storageService.saveUserCourse(uid, course)
    }

    override suspend fun updateUserCourse(course: CourseUiModel) {
        val uid = accountService.currentUserId
        storageService.updateUserCourse(uid, course)
    }

    override suspend fun deleteUserCourse(courseId: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            courseDao.deleteCourseById(courseId, now)
            courseDao.bulkDeleteChannelsForCourse(courseId, now)
            courseDao.bulkDeleteVideosForCourse(courseId, now)
        }
        schedulePushSync()
    }

    // label
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLabelsFlow(): Flow<List<LabelEntity>> {
        return labelDao.getAllLabels()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getLabelById(labelId: String): LabelEntity? {
        return labelDao.getLabelById(labelId)
    }

    override suspend fun saveLabel(courseId: String, label: UserLabel) {
        val userId = accountService.currentUserId
        val operationTime = Timestamp.now()
        val updatedLabel = label.copy(timestamp = operationTime)

        labelDao.insertOrUpdate(listOf(updatedLabel))
        storageService.saveUserLabel(userId, courseId, updatedLabel)
    }

    override suspend fun deleteLabel(labelId: String) {
        labelDao.deleteLabelById(labelId)
        schedulePushSync()
    }

    // stats
    override fun getMonthlyStatsFlow(
        userCourseId: String, monthId: String
    ): Flow<UserMonthlyStats?> {
        val uid = accountService.currentUserId
        return storageService.getMonthlyStatsFlow(uid, userCourseId, monthId)
    }


    // preferences
    override suspend fun saveThemeMode(theme: AppTheme) {
        val uid = accountService.currentUserId
        preferenceStorageService.saveThemeMode(uid, theme)
    }

    override suspend fun saveConfettiColors(colors: ConfettiOptions) {
        val uid = accountService.currentUserId
        preferenceStorageService.saveConfettiColors(uid, colors)
    }

    override suspend fun setCurrentCourse(courseId: String) {
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
            "global_push_sync", ExistingWorkPolicy.APPEND_OR_REPLACE, listOf(pushSync)
        ).enqueue()
    }
}