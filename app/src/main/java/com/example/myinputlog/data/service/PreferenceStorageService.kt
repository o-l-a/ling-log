package com.example.myinputlog.data.service

import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface PreferenceStorageService {
    fun currentCourseId(userId: String): Flow<String>
    fun themeMode(userId: String): Flow<AppTheme>
    fun confettiColors(userId: String): Flow<ConfettiOptions>
    suspend fun saveCurrentCourseId(userId: String, courseId: String)
    suspend fun clearCurrentCourseId(userId: String)
    suspend fun saveThemeMode(userId: String, theme: AppTheme)
    suspend fun saveConfettiColors(userId: String, colors: ConfettiOptions)
    suspend fun getLastPullTimestamp(userId: String): Long
    suspend fun saveLastPullTimestamp(userId: String, timestamp: Long)
}