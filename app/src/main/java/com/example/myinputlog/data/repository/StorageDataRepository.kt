package com.example.myinputlog.data.repository

import androidx.paging.PagingData
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import kotlinx.coroutines.flow.Flow

interface StorageDataRepository {
    val userCourses: Flow<List<UserCourse>?>
    val currentCourseId: Flow<String>
    fun videoPagingFlow(courseId: String): Flow<PagingData<YouTubeVideo>>
    fun channelPagingFlow(courseId: String): Flow<PagingData<YouTubeChannel>>
    suspend fun getYouTubeVideo(courseId: String, videoId: String): YouTubeVideo?
    suspend fun getYouTubeChannel(courseId: String, channelId: String): YouTubeChannel?
    suspend fun getCourseStatistics(courseId: String): CourseStatistics
    suspend fun saveVideo(
        courseId: String,
        video: YouTubeVideo,
        originalVideo: YouTubeVideo?,
        channel: YouTubeChannel?
    )

    suspend fun deleteVideo(courseId: String, video: YouTubeVideo)
    suspend fun setCurrentCourse(courseId: String)
}