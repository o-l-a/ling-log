package com.example.myinputlog.ui.screens.media_list

import com.example.myinputlog.ui.models.LabelUiModel

sealed interface MediaListUiState {
    data object Loading : MediaListUiState
    data object Empty : MediaListUiState
    data object Error : MediaListUiState
    data object NetworkError : MediaListUiState
    data class Success(
        val currentCourseId: String = "",
        val allLabels: Set<LabelUiModel> = emptySet(),
        val filters: MediaFilters = MediaFilters()
    ) : MediaListUiState
}

data class MediaFilters(
    val searchQuery: String = "",
    val selectedChannels: Set<String> = emptySet(),
    val selectedLabels: Set<String> = emptySet(),
    val allChannelsSelected: Boolean = false,
    val allLabelsSelected: Boolean = false
) {
    fun hasActiveFilters(isChannel: Boolean = false): Boolean {
        val common = searchQuery.isNotEmpty() || selectedLabels.isNotEmpty()
        return if (isChannel) common else common || selectedChannels.isNotEmpty()
    }
}