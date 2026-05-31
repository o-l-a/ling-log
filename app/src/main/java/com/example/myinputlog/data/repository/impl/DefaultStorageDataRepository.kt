package com.example.myinputlog.data.repository.impl

import android.util.Log
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.myinputlog.data.local.dao.LabelDao
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.data.model.UserLabel
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.paging.FirestorePagingSource
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.data.service.StorageService
import com.example.myinputlog.data.utils.createReactivePagingFlow
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.Date
import javax.inject.Inject

class DefaultStorageDataRepository @Inject constructor(
    private val storageService: StorageService,
    private val accountService: AccountService,
    private val preferenceStorageService: PreferenceStorageService,
    private val pagingConfig: PagingConfig,
    private val labelDao: LabelDao
) : StorageDataRepository {
    companion object {
        private const val TAG = "StorageRepository"
    }

    private val userId: Flow<String> = accountService.currentUser.map { it.id }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val userCourses: Flow<List<UserCourse>?> = userId.flatMapLatest { id ->
        if (id.isEmpty()) {
            flowOf(null)
        } else {
            storageService.getUserCourses(id)
        }
    }

    override val currentUser: Flow<UserData> = accountService.currentUser

    override val themeMode: Flow<AppTheme> = preferenceStorageService.themeMode

    override val confettiColors: Flow<ConfettiOptions> = preferenceStorageService.confettiColors

    override suspend fun changeUsername(newUsername: String) {
        accountService.changeUsername(newUsername)
    }

    override suspend fun signOut() {
        accountService.signOut()
    }

    override suspend fun deleteAccount() {
        accountService.deleteAccount()
    }

    override val currentCourseId: Flow<String> = preferenceStorageService.currentCourseId

    override suspend fun setCurrentCourse(courseId: String) {
        preferenceStorageService.saveCurrentCourseId(courseId)
    }

    override suspend fun saveThemeMode(theme: AppTheme) {
        preferenceStorageService.saveThemeMode(theme)
    }

    override suspend fun saveConfettiColors(colors: ConfettiOptions) {
        preferenceStorageService.saveConfettiColors(colors)
    }

    override fun videoPagingFlow(courseId: String): Flow<PagingData<YouTubeVideo>> {
        return userId.createReactivePagingFlow(
            courseId = courseId,
            changeSignal = { uid -> storageService.getVideosChangeSignal(uid, courseId) },
            factoryProvider = { uid ->
                FirestorePagingSource(YouTubeVideo::class.java) { key, loadSize ->
                    storageService.videosByWatchedOnQuery(uid, courseId, key, loadSize)
                }
            },
            pagingConfig = pagingConfig
        )
    }

    override fun channelPagingFlow(courseId: String): Flow<PagingData<YouTubeChannel>> {
        return userId.createReactivePagingFlow(
            courseId = courseId,
            changeSignal = { uid -> storageService.getVideosChangeSignal(uid, courseId) },
            factoryProvider = { uid ->
                FirestorePagingSource(YouTubeChannel::class.java) { key, loadSize ->
                    storageService.channelsByVideoCount(uid, courseId, key, loadSize)
                }
            },
            pagingConfig = pagingConfig
        )
    }

    override suspend fun getYouTubeVideo(courseId: String, videoId: String): YouTubeVideo? {
        val uid = accountService.currentUserId
        return storageService.getYouTubeVideo(uid, courseId, videoId)
    }

    override suspend fun getYouTubeChannel(courseId: String, channelId: String): YouTubeChannel? {
        if (channelId.isBlank()) {
            return null
        }
        val uid = accountService.currentUserId
        return storageService.getYouTubeChannel(uid, courseId, channelId)
    }

    override suspend fun getUserCourse(courseId: String): UserCourse? {
        val uid = accountService.currentUserId
        return storageService.getUserCourse(uid, courseId)
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
    override fun getLabelsFlow(userCourseId: String): Flow<List<UserLabel>> {
        val userId = accountService.currentUserId
        return storageService.getLabelsChangeSignal(userId, userCourseId)
            .onEach { remoteTimestamp ->
                syncLabels(userCourseId, remoteTimestamp)
            }.flatMapLatest {
                labelDao.getLabelsFlow()
            }
    }

    suspend fun syncLabels(userCourseId: String, remoteTimestamp: Timestamp?) {
        try {
            val userId = accountService.currentUserId
            val latestLocalTime = labelDao.getLatestTimestamp()

            if (remoteTimestamp != null && latestLocalTime != null) {
                if (remoteTimestamp.seconds <= latestLocalTime.seconds) {
                    Log.d(TAG, "Skipping label sync: local is up to date.")
                    return
                }
            }

            val updatedLabels = storageService.getLabelsUpdatedAfter(
                userId, userCourseId, latestLocalTime ?: Timestamp(Date(0))
            )
            if (updatedLabels.isNotEmpty()) {
                labelDao.insertOrUpdate(updatedLabels)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync labels", e)
        }
    }

    override suspend fun saveVideo(
        courseId: String,
        video: YouTubeVideo,
        originalVideo: YouTubeVideo?,
        channel: YouTubeChannel?
    ) {
        val operationTimestamp = Timestamp.now()

        storageService.saveYouTubeVideo(
            userId = accountService.currentUserId,
            courseId = courseId,
            newVideo = video,
            oldVideo = originalVideo,
            timestamp = operationTimestamp,
            channelMetadata = channel
        )

        originalVideo?.labelIds?.forEach { labelId ->
            labelDao.incrementStats(
                labelId, -originalVideo.durationInSeconds, -1L, operationTimestamp
            )
        }

        video.labelIds.forEach { labelId ->
            labelDao.incrementStats(labelId, video.durationInSeconds, 1L, operationTimestamp)
        }
    }

    override suspend fun deleteVideo(courseId: String, video: YouTubeVideo) {
        storageService.deleteYouTubeVideo(accountService.currentUserId, courseId, video)
    }
}