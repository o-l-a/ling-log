package com.example.myinputlog.data.service

import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.ui.screens.common.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface PreferenceStorageService {
    fun currentCourseId(userId: String): Flow<String>
    fun themeMode(userId: String): Flow<AppTheme>
    fun confettiColors(userId: String): Flow<ConfettiOptions>
    fun channelSortDefault(userId: String): Flow<SortOptions>
    fun videoSortDefault(userId: String): Flow<SortOptions>
    suspend fun saveCurrentCourseId(userId: String, courseId: String)
    suspend fun clearCurrentCourseId(userId: String)
    suspend fun saveThemeMode(userId: String, theme: AppTheme)
    suspend fun saveConfettiColors(userId: String, colors: ConfettiOptions)
    suspend fun saveChannelSortDefault(userId: String, sort: SortOptions)
    suspend fun saveVideoSortDefault(userId: String, sort: SortOptions)
    suspend fun getLastPullTimestamp(userId: String): Long
    suspend fun saveLastPullTimestamp(userId: String, timestamp: Long)
}