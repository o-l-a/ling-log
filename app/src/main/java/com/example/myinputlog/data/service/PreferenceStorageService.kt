package com.example.myinputlog.data.service

interface PreferenceStorageService {
    suspend fun saveCurrentCourseId(courseId: String)
}