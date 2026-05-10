package com.example.myinputlog.ui.screens.video_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.paging.VideoPagingSource
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.models.mapToCourseHeader
import com.example.myinputlog.ui.screens.home.StatsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    accountService: AccountService,
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService,
    private val pagingSourceFactory: VideoPagingSource.Factory,
    private val pagingConfig: PagingConfig
) : ViewModel() {
    private val userCoursesFlow = storageService.userCourses
    private val userIdFlow = accountService.currentUser.map { it.id }
    private val currentIdFlow = preferenceStorageService.currentCourseId

    private val sessionFlow = combine(
        userIdFlow, currentIdFlow
    ) { uid, cid -> uid to cid }

    val currentCourseId: StateFlow<String> = preferenceStorageService.currentCourseId.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val videoFlow = sessionFlow.flatMapLatest { (uid, cid) ->
        if (uid.isEmpty() || cid.isEmpty()) {
            flowOf(PagingData.empty())
        } else {
            Pager(config = pagingConfig) {
                pagingSourceFactory.create(userId = uid, courseId = cid)
            }.flow
        }
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val statsWorker = sessionFlow.flatMapLatest { (uid, cid) ->
        flow {
            emit(StatsResult.Loading)
            try {
                val stats = storageService.getCourseStatistics(uid, cid)
                emit(StatsResult.Success(stats))
            } catch (e: Exception) {
                emit(StatsResult.Error(e))
            }
        }
    }

    val videoListUiState: StateFlow<VideoListUiState> = combine(
        userCoursesFlow, currentIdFlow, statsWorker
    ) { courses, id, statsRes ->

        when {
            courses == null -> VideoListUiState.Loading
            courses.isEmpty() -> VideoListUiState.Empty
            statsRes is StatsResult.Error -> VideoListUiState.NetworkError

            else -> {
                val current = courses.find { it.id == id } ?: courses.first()
                val courseStatistics =
                    (statsRes as? StatsResult.Success)?.stats ?: CourseStatistics()
                val courseHeader = mapToCourseHeader(current, courseStatistics)

                VideoListUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses,
                    courseStatistics = courseStatistics,
                    videos = videoFlow.insertHeaderAndSeparators()
                )
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // Save battery
        initialValue = VideoListUiState.Loading
    )

    fun changeCurrentCourseId(newCourse: UserCourse) {
        viewModelScope.launch {
            preferenceStorageService.saveCurrentCourseId(newCourse.id)
        }
    }

    companion object {
        private const val TAG = "VideoListViewModel"
    }
}

fun Flow<PagingData<YouTubeVideo>>.insertHeaderAndSeparators(): Flow<PagingData<YouTubeVideo>> {
    return this.map {
        it.insertSeparators { before: YouTubeVideo?, after: YouTubeVideo? ->
            when {
                before == null && after != null -> {
                    YouTubeVideo(watchedOn = after.watchedOn)
                }

                before == null || after == null -> {
                    null
                }

                before.watchedOn != after.watchedOn -> {
                    YouTubeVideo(watchedOn = after.watchedOn)
                }

                else -> {
                    null
                }
            }
        }
    }
}