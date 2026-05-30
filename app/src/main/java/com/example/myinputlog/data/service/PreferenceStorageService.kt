package com.example.myinputlog.data.service

import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface PreferenceStorageService {
    val currentCourseId: Flow<String>
    val themeMode: Flow<AppTheme>
    val confettiColors: Flow<ConfettiOptions>
    suspend fun saveCurrentCourseId(courseId: String)
    suspend fun saveThemeMode(theme: AppTheme)
    suspend fun saveConfettiColors(colors: ConfettiOptions)
}