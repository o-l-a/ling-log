package com.example.myinputlog.data.repository.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.paging.VideoPagingSource
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.data.service.StorageService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultStorageDataRepository @Inject constructor(
    private val storageService: StorageService,
    private val accountService: AccountService,
    private val pagingSourceFactory: VideoPagingSource.Factory,
    private val preferenceStorageService: PreferenceStorageService,
    private val pagingConfig: PagingConfig
) : StorageDataRepository {
    private val userId: Flow<String> = accountService.currentUser.map { it.id }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val userCourses: Flow<List<UserCourse>?> = userId.flatMapLatest { id ->
        if (id.isEmpty()) {
            flowOf(null)
        } else {
            storageService.getUserCourses(id)
        }
    }

    override val currentCourseId: Flow<String> = preferenceStorageService.currentCourseId

    override suspend fun setCurrentCourse(courseId: String) {
        preferenceStorageService.saveCurrentCourseId(courseId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun videoPagingFlow(courseId: String): Flow<PagingData<YouTubeVideo>> {
        return userId.flatMapLatest { uid ->
            if (uid.isEmpty() || courseId.isEmpty()) {
                flowOf(PagingData.empty())
            } else {
                Pager(config = pagingConfig) {
                    pagingSourceFactory.create(userId = uid, courseId = courseId)
                }.flow
            }
        }
    }

    override suspend fun getYouTubeVideo(courseId: String, videoId: String): YouTubeVideo? {
        val uid = accountService.currentUserId
        return storageService.getYouTubeVideo(uid, courseId, videoId)
    }

    override suspend fun getYouTubeChannel(courseId: String, channelId: String): YouTubeChannel? {
        if (channelId.isBlank()) {
            return null
        }
        val uid = accountService.currentUserId
        return storageService.getYouTubeChannel(uid, courseId, channelId)
    }

    override suspend fun getCourseStatistics(courseId: String): CourseStatistics {
        val uid = accountService.currentUserId
        return storageService.getCourseStatistics(uid, courseId)
    }

    override suspend fun saveVideo(
        courseId: String,
        video: YouTubeVideo,
        originalVideo: YouTubeVideo?,
        channel: YouTubeChannel?
    ) {
        storageService.saveYouTubeVideo(
            userId = accountService.currentUserId,
            courseId = courseId,
            newVideo = video,
            oldVideo = originalVideo,
            channelMetadata = channel
        )
    }

    override suspend fun deleteVideo(courseId: String, video: YouTubeVideo) {
        storageService.deleteYouTubeVideo(accountService.currentUserId, courseId, video)
    }
}