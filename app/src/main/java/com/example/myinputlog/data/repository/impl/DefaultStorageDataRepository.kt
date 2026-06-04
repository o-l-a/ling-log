package com.example.myinputlog.data.repository.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.example.myinputlog.data.local.dao.ChannelDao
import com.example.myinputlog.data.local.dao.CourseDao
import com.example.myinputlog.data.local.dao.LabelDao
import com.example.myinputlog.data.local.dao.VideoDao
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
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.data.worker.PushSyncWorker
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.screens.utils.ext.stripUrl
import com.example.myinputlog.ui.screens.video.VideoMetadata
import com.example.myinputlog.ui.screens.video.VideoUserDraft
import com.example.myinputlog.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class DefaultStorageDataRepository @Inject constructor(
    private val accountService: AccountService,
    private val preferenceStorageService: PreferenceStorageService,
    private val pagingConfig: PagingConfig,
    private val workManager: WorkManager,
    private val courseDao: CourseDao,
    private val labelDao: LabelDao,
    private val videoDao: VideoDao,
    private val channelDao: ChannelDao
) : StorageDataRepository {
    companion object {
        private const val TAG = "StorageRepository"
    }

    private val userId: Flow<String> = accountService.currentUser.map { it.id }

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

    override suspend fun changeUsername(newUsername: String) {
        accountService.changeUsername(newUsername)
    }

    override suspend fun signOut() {
        accountService.signOut()
    }

    override suspend fun deleteAccount() {
        accountService.deleteAccount()
    }


    override suspend fun setCurrentCourse(courseId: String) {
        val uid = accountService.currentUserId
        preferenceStorageService.saveCurrentCourseId(uid, courseId)
    }

    override suspend fun saveThemeMode(theme: AppTheme) {
        val uid = accountService.currentUserId
        preferenceStorageService.saveThemeMode(uid, theme)
    }

    override suspend fun saveConfettiColors(colors: ConfettiOptions) {
        val uid = accountService.currentUserId
        preferenceStorageService.saveConfettiColors(uid, colors)
    }

    override fun videoPagingFlow(courseId: String): Flow<PagingData<VideoWithChannelAndLabels>> {
        return Pager(
            config = pagingConfig, pagingSourceFactory = {
                videoDao.getVideosPagingSource(courseId)
            }).flow
    }

    override fun channelPagingFlow(courseId: String): Flow<PagingData<ChannelWithLabels>> {
        return Pager(
            config = pagingConfig, pagingSourceFactory = {
                channelDao.getChannelsPagingSource(courseId)
            }).flow
    }

    override suspend fun getVideo(videoId: String): VideoWithChannelAndLabels? {
        return videoDao.getVideoWithChannelAndLabelsById(videoId)
    }

    override suspend fun getChannel(channelId: String): ChannelWithLabels? {
        return channelDao.getChannelWithLabelsById(channelId)
    }

    override suspend fun getUserCourse(courseId: String): CourseEntity? {
        return courseDao.getCourseById(courseId)
    }

    override suspend fun saveUserCourse(course: UserCourse): String {
        val uid = accountService.currentUserId
        return storageService.saveUserCourse(uid, course)
    }

    override suspend fun updateUserCourse(course: UserCourse) {
        val uid = accountService.currentUserId
        storageService.updateUserCourse(uid, course)
    }

    override suspend fun deleteUserCourse(courseId: String) {
        val uid = accountService.currentUserId
        storageService.deleteUserCourse(uid, courseId)
    }

    override fun getMonthlyStatsFlow(
        userCourseId: String, monthId: String
    ): Flow<UserMonthlyStats?> {
        val uid = accountService.currentUserId
        return storageService.getMonthlyStatsFlow(uid, userCourseId, monthId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLabelsFlow(): Flow<List<LabelEntity>> {
        return labelDao.getAllLabels()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getLabelById(labelId: String): LabelEntity? {
        return labelDao.getLabelById(labelId)
    }

    suspend fun saveVideo(id: String, draft: VideoUserDraft, metadata: VideoMetadata) {
        val existingVideo = videoDao.getVideoWithChannelAndLabelsById(id)
        val now = System.currentTimeMillis()

        val videoToSave = existingVideo?.copy(
            watchedOn = draft.watchedOn ?: Date(0),
            speakersNationality = draft.speakersNationality,
            videoUrl = draft.videoUrl.stripUrl(),
            lastUpdated = now
            // TODO
        ) ?: VideoWithChannelAndLabels(
            id = UUID.randomUUID().toString(),
            title = metadata.title,
            videoUrl = draft.videoUrl.stripUrl(),
            watchedOn = draft.watchedOn ?: Date(0),
            lastUpdated = now
            // TODO
        )

        videoDao.upsertVideoWithLabelIds(videoToSave)
    }

    override suspend fun saveVideo(
        courseId: String,
        video: YouTubeVideo,
        originalVideo: YouTubeVideo?,
        channel: YouTubeChannel?
    ) {
        val userId = accountService.currentUserId
        val operationTimestamp = Timestamp.now()

        storageService.saveYouTubeVideo(
            userId = userId,
            courseId = courseId,
            newVideo = video,
            oldVideo = originalVideo,
            timestamp = operationTimestamp,
            channelMetadata = channel
        )

        originalVideo?.labelIds?.forEach { labelId ->
            labelDao.incrementStats(
                userId, courseId, labelId, -originalVideo.durationInSeconds, -1L, operationTimestamp
            )
        }

        video.labelIds.forEach { labelId ->
            labelDao.incrementStats(
                userId, courseId, labelId, video.durationInSeconds, 1L, operationTimestamp
            )
        }
    }

    override suspend fun deleteVideo(courseId: String, video: YouTubeVideo) {
        val userId = accountService.currentUserId
        val operationTimestamp = Timestamp.now()

        storageService.deleteYouTubeVideo(
            userId, courseId, video, operationTimestamp
        )

        video.labelIds.forEach { labelId ->
            labelDao.incrementStats(
                userId, courseId, labelId, -video.durationInSeconds, -1L, operationTimestamp
            )
        }
    }

    override suspend fun saveLabel(courseId: String, label: UserLabel) {
        val userId = accountService.currentUserId
        val operationTime = Timestamp.now()
        val updatedLabel = label.copy(timestamp = operationTime)

        labelDao.insertOrUpdate(listOf(updatedLabel))
        storageService.saveUserLabel(userId, courseId, updatedLabel)
    }

    override suspend fun deleteLabel(courseId: String, label: UserLabel) {
        val userId = accountService.currentUserId
        labelDao.deleteLabelById(userId, courseId, label.id)
        storageService.deleteUserLabel(userId, courseId, label, operationTime)
    }

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