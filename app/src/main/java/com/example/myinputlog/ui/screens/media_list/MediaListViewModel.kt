package com.example.myinputlog.ui.screens.media_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.models.mapToCourseUiModel
import com.example.myinputlog.ui.models.toCourseUiModel
import com.example.myinputlog.ui.screens.utils.ext.asStartOfDay
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
            courses.isEmpty() -> MediaListUiState.Empty

            else -> {
                val current = courses.find { it.course.id == id } ?: courses.first()
                val courseHeader = mapToCourseUiModel(current.toCourseUiModel())

                MediaListUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses.map { it.toCourseUiModel() }
                )
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000),
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

fun Flow<PagingData<VideoUiModel>>.insertHeaderAndSeparators(): Flow<PagingData<VideoUiModel>> {
    return this.map {
        it.insertSeparators { before: VideoUiModel?, after: VideoUiModel? ->
            when {
                before == null && after != null -> {
                    VideoUiModel(watchedOn = after.watchedOn)
                }

                before == null || after == null -> {
                    null
                }

                before.watchedOn.asStartOfDay() != after.watchedOn.asStartOfDay() -> {
                    VideoUiModel(watchedOn = after.watchedOn)
                }

                else -> {
                    null
                }
            }
        }
    }
}