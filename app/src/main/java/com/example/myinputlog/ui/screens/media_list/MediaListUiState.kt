package com.example.myinputlog.ui.screens.media_list

import com.example.myinputlog.ui.models.CourseHeaderUiModel
import com.example.myinputlog.ui.models.CourseUiModel

sealed interface MediaListUiState {
    data object Loading : MediaListUiState
    data object Empty : MediaListUiState
    data object Error : MediaListUiState
    data object NetworkError : MediaListUiState
    data class Success(
        val courseHeader: CourseHeaderUiModel = CourseHeaderUiModel(),
        val userCourses: List<CourseUiModel> = listOf(),
        val filters: MediaFilters = MediaFilters()
    ) : MediaListUiState
}

data class MediaFilters(
    val searchQuery: String = "",
    val selectedChannels: Set<String> = emptySet(),
    val selectedLabels: Set<String> = emptySet()
)