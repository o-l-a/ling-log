package com.example.myinputlog.ui.screens.media_list

import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.models.CourseHeaderUiModel

sealed interface MediaListUiState {
    data object Loading : MediaListUiState
    data object Empty : MediaListUiState
    data object Error : MediaListUiState
    data object NetworkError : MediaListUiState
    data class Success(
        val courseHeader: CourseHeaderUiModel = CourseHeaderUiModel(),
        val userCourses: List<CourseUiModel> = listOf()
    ) : MediaListUiState
}