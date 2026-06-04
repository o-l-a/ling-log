package com.example.myinputlog.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myinputlog.data.service.AccountService
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class SyncManager @Inject constructor(
    private val workManagerLazy: dagger.Lazy<WorkManager>,
    private val accountService: AccountService
) {
    companion object {
        private const val PUSH_PERIODIC_WORK_NAME = "periodic_push_sync"
        private const val PULL_ONE_TIME_WORK_NAME = "one_time_pull_sync"
    }

    private val workManager get() = workManagerLazy.get()

    fun triggerImmediatePull() {
        val userId = accountService.currentUserId
        if (userId.isEmpty() || userId == "guest") return

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val pullRequest =
            OneTimeWorkRequestBuilder<PullSyncWorker>().setConstraints(constraints).build()

        workManager.enqueueUniqueWork(
            PULL_ONE_TIME_WORK_NAME, ExistingWorkPolicy.REPLACE, pullRequest
        )
    }

    fun setupBackgroundSync() {
        val userId = accountService.currentUserId
        if (userId.isEmpty()) return

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true).build()

        val pushRequest = PeriodicWorkRequestBuilder<PushSyncWorker>(
            4, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        workManager.enqueueUniquePeriodicWork(
            PUSH_PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, pushRequest
        )
    }

    fun stopAllSync() {
        workManager.cancelAllWork()
    }
}