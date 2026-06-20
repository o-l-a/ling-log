package com.example.myinputlog.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myinputlog.data.local.model.SyncPointers
import com.example.myinputlog.data.local.toEntity
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.AppDatabaseManager
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.data.service.StorageService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

@HiltWorker
class PullSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dbManager: AppDatabaseManager,
    private val accountService: AccountService,
    private val preferences: PreferenceStorageService,
    private val storageService: StorageService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userId = accountService.currentUserId
        val lastPull = preferences.getLastPullTimestamp(userId)
        Log.d(TAG, "Last pull: ${Date(lastPull)}")

        try {
            val db = dbManager.getDatabase(userId)

            val videoDao = db.videoDao()
            val courseDao = db.courseDao()
            val labelDao = db.labelDao()
            val channelDao = db.channelDao()

            val pointers = storageService.getSyncPointers(userId)?.toDomain() ?: SyncPointers()
            Log.d(TAG, "Sync pointers: labels ${Date(pointers.labelsLastUpdated)}")
            Log.d(TAG, "Sync pointers: channels ${Date(pointers.channelsLastUpdated)}")
            Log.d(TAG, "Sync pointers: courses ${Date(pointers.coursesLastUpdated)}")

            if (pointers.coursesLastUpdated > lastPull) {
                val courses = storageService.getLastUpdatedCourses(userId, Date(lastPull))
                courseDao.bulkUpsertCoursesIfNewer(courses.map { it.toEntity() })
                Log.d(TAG, "${courses.count()} courses upserted.")
            } else {
                Log.d(TAG, "No updated courses found.")
            }

            if (pointers.labelsLastUpdated > lastPull) {
                val labels = storageService.getLastUpdatedLabels(userId).map { it.toEntity() }
                if (labels.isNotEmpty()) {
                    labelDao.bulkUpsertLabelsIfNewer(labels)
                    Log.d(TAG, "${labels.count()} labels upserted.")
                }
            } else {
                Log.d(TAG, "No updated labels found.")
            }

            if (pointers.channelsLastUpdated > lastPull) {
                val channels = storageService.getLastUpdatedChannels(userId, Date(lastPull))
                    .map { it.toEntity() }
                channelDao.bulkUpsertChannelsWithLabelIdsIfNewer(channels)
                Log.d(TAG, "${channels.count()} channels upserted.")
            } else {
                Log.d(TAG, "No updated channels found.")
            }

            val videos =
                storageService.getLastUpdatedVideos(userId, Date(lastPull)).map { it.toEntity() }
            if (videos.isNotEmpty()) {
                videoDao.bulkUpsertVideosWithLabelIdsIfNewer(videos)
                Log.d(TAG, "${videos.count()} videos upserted.")
            } else {
                Log.d(TAG, "No updated videos found.")
            }

            preferences.saveLastPullTimestamp(userId, System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Pull failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "PullSyncWorker"
    }
}