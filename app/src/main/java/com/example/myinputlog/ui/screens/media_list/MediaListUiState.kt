package com.example.myinputlog.ui.screens.media_list

import androidx.paging.PagingData
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.models.CourseHeaderUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed interface MediaListUiState {
    data object Loading : MediaListUiState
    data object Empty : MediaListUiState
    data object Error : MediaListUiState
    data object NetworkError : MediaListUiState
    data class Success(
        val courseHeader: CourseHeaderUiModel = CourseHeaderUiModel(),
        val userCourses: List<UserCourse> = listOf(),
        val videos: Flow<PagingData<YouTubeVideo>> = emptyFlow(),
        val channels: Flow<PagingData<YouTubeChannel>> = emptyFlow()
    ) : MediaListUiState
}