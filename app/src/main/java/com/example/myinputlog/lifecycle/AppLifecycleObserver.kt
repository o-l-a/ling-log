package com.example.myinputlog.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.worker.SyncManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val syncManager: SyncManager,
    private val accountService: AccountService
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        if (accountService.currentUserId.isNotBlank()) {
            syncManager.setupBackgroundSync()
            syncManager.triggerImmediatePull()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
    }
}