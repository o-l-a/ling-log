package com.example.myinputlog.data.service

import android.content.Context
import androidx.room.Room
import com.example.myinputlog.data.UnauthenticatedAccessException
import com.example.myinputlog.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AppDatabaseManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val accountService: AccountService
) {
    private var instance: AppDatabase? = null
    private var currentUserId: String? = null

    fun getDatabase(): AppDatabase {
        val userId = accountService.currentUserId

        if (userId.isBlank()) {
            throw UnauthenticatedAccessException()
        }

        if (userId != currentUserId) {
            synchronized(this) {
                if (userId != currentUserId) {
                    instance?.close()
                    instance = null
                    currentUserId = userId
                }
            }
        }

        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context, AppDatabase::class.java, "user_db_$userId.db"
            ).fallbackToDestructiveMigration(true).build().also { instance = it }
        }
    }
}