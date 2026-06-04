package com.example.myinputlog.ui.screens.media_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.mapToCourseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val repository: StorageDataRepository,
) : ViewModel() {
    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val videoFlow = currentCourseId.flatMapLatest { cid ->
        repository.videoPagingFlow(cid).insertHeaderAndSeparators()
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val channelFlow = currentCourseId.flatMapLatest { cid ->
        repository.channelPagingFlow(cid)
    }.cachedIn(viewModelScope)

    val mediaListUiState: StateFlow<MediaListUiState> = combine(
        repository.courses, currentCourseId
    ) { courses, id ->

        when {
            courses == null -> MediaListUiState.Loading
            courses.isEmpty() -> MediaListUiState.Empty

            else -> {
                val current = courses.find { it.id == id } ?: courses.first()
                val courseHeader = mapToCourseUiModel(current)

                MediaListUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses
                )
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // Save battery
        initialValue = MediaListUiState.Loading
    )

    fun changeCurrentCourseId(newCourse: CourseUiModel) {
        viewModelScope.launch {
            repository.setCurrentCourse(newCourse.id)
        }
    }

    companion object {
        private const val TAG = "MediaListViewModel"
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