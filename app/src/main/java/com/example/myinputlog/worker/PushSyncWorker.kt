package com.example.myinputlog.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myinputlog.data.local.dao.ChannelDao
import com.example.myinputlog.data.local.dao.CourseDao
import com.example.myinputlog.data.local.dao.LabelDao
import com.example.myinputlog.data.local.dao.VideoDao
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.StorageService
import com.example.myinputlog.data.utils.DateUtils.toMonthKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class PushSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val videoDao: VideoDao,
    private val courseDao: CourseDao,
    private val labelDao: LabelDao,
    private val channelDao: ChannelDao,
    private val accountService: AccountService,
    private val storageService: StorageService
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userId = accountService.currentUserId
        if (userId.isEmpty()) return@withContext Result.failure()

        val videoResult = syncVideos(userId)
        val channelResult = syncChannels(userId)
        val metadataResult = syncMetadata(userId)

        return@withContext if (videoResult && channelResult && metadataResult) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    private suspend fun syncVideos(userId: String): Boolean = try {
        val unsyncedVideos = videoDao.getUnsyncedVideosWithLabelIds()
        if (unsyncedVideos.isEmpty()) {
            Log.d(TAG, "No videos to sync. Skipping.")
            true
        } else {
            Log.d(TAG, "Pushing ${unsyncedVideos.count()} channels.")
            val groupedByMonth = unsyncedVideos.groupBy { it.video.watchedOn.toMonthKey() }
            storageService.pushMonths(userId, groupedByMonth)
            val videoIds = unsyncedVideos.map { it.video.id }
            videoDao.markVideosSynced(videoIds)
            true
        }
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        false
    }

    private suspend fun syncChannels(userId: String) = try {
        val unsyncedChannels = channelDao.getUnsyncedChannelsWithLabelIds()
        if (unsyncedChannels.isEmpty()) {
            Log.d(TAG, "No channels to sync. Skipping.")
            true
        } else {
            Log.d(TAG, "Pushing ${unsyncedChannels.count()} channels.")
            storageService.pushChannels(userId, unsyncedChannels)
            val channelIds = unsyncedChannels.map { it.channel.id }
            channelDao.markChannelsSynced(channelIds)
            true
        }
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        false
    }

    private suspend fun syncMetadata(userId: String) = try {
        val unsyncedCourses = courseDao.getUnsyncedCourses()
        val unsyncedLabels = labelDao.getUnsyncedLabels()

        if (unsyncedCourses.isEmpty() && unsyncedLabels.isEmpty()) {
            Log.d(TAG, "No metadata to sync. Skipping.")
            true
        } else {
            Log.d(TAG, "Syncing metadata (${unsyncedCourses.count()} courses).")
            val allLabels =
                if (unsyncedLabels.isNotEmpty()) labelDao.getAllLabelsAsList() else emptyList()
            storageService.pushMetadata(userId, unsyncedCourses, allLabels)
            val courseIds = unsyncedCourses.map { it.id }
            val labelIds = unsyncedLabels.map { it.id }
            courseDao.markCoursesSynced(courseIds)
            labelDao.markLabelsSynced(labelIds)
            true
        }
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        false
    }

    companion object {
        private const val TAG = "PushSyncWorker"
    }
}