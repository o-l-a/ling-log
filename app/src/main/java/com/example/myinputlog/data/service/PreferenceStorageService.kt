package com.example.myinputlog.data.service

import kotlinx.coroutines.flow.Flow

interface PreferenceStorageService {
    val currentCourseId: Flow<String>
    suspend fun saveCurrentCourseId(courseId: String)
}