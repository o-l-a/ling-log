package com.example.myinputlog.ui.screens.video_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.mapToCourseUiModel
import com.example.myinputlog.ui.screens.home.StatsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val repository: StorageDataRepository,
) : ViewModel() {
    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val videoFlow = currentCourseId.flatMapLatest { cid ->
        repository.videoPagingFlow(cid)
            .insertHeaderAndSeparators()
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val statsWorker = currentCourseId.flatMapLatest { cid ->
        flow {
            emit(StatsResult.Loading)
            try {
                val stats = repository.getCourseStatistics(cid)
                emit(StatsResult.Success(stats))
            } catch (e: Exception) {
                emit(StatsResult.Error(e))
            }
        }
    }

    val videoListUiState: StateFlow<VideoListUiState> = combine(
        repository.userCourses, currentCourseId, statsWorker
    ) { courses, id, statsRes ->

        when {
            courses == null -> VideoListUiState.Loading
            courses.isEmpty() -> VideoListUiState.Empty
            statsRes is StatsResult.Error -> VideoListUiState.NetworkError

            else -> {
                val current = courses.find { it.id == id } ?: courses.first()
                val courseStatistics =
                    (statsRes as? StatsResult.Success)?.stats ?: CourseStatistics()
                val courseHeader = mapToCourseUiModel(current, courseStatistics)

                VideoListUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses,
                    courseStatistics = courseStatistics,
                    videos = videoFlow
                )
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // Save battery
        initialValue = VideoListUiState.Loading
    )

    fun changeCurrentCourseId(newCourse: UserCourse) {
        viewModelScope.launch {
            repository.setCurrentCourse(newCourse.id)
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