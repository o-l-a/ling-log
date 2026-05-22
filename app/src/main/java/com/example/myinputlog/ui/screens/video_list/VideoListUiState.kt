package com.example.myinputlog.ui.screens.video_list

import androidx.paging.PagingData
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.models.CourseHeaderUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed interface VideoListUiState {
    data object Loading : VideoListUiState
    data object Empty : VideoListUiState
    data object Error : VideoListUiState
    data object NetworkError : VideoListUiState
    data class Success(
        val courseHeader: CourseHeaderUiModel = CourseHeaderUiModel(),
        val userCourses: List<UserCourse> = listOf(),
        val videos: Flow<PagingData<YouTubeVideo>> = emptyFlow(),
    ) : VideoListUiState
}