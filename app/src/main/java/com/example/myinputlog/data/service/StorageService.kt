package com.example.myinputlog.data.service

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import kotlinx.coroutines.flow.Flow

interface StorageService {
    val userCourses: Flow<List<UserCourse>>

    suspend fun getUserCourse(userCourseId: String): UserCourse?
    suspend fun saveUserCourse(userCourse: UserCourse)
    suspend fun updateUserCourse(userCourse: UserCourse)
    suspend fun deleteUserCourse(userCourseId: String)

    suspend fun getYouTubeVideo(youTubeVideoId: String): YouTubeVideo?
    suspend fun saveYouTubeVideo(youTubeVideo: YouTubeVideo)
    suspend fun updateYouTubeVideo(youTubeVideo: YouTubeVideo)
    suspend fun deleteYouTubeVideo(youTubeVideoId: String)

    suspend fun deleteAllForUser(userId: String)
}