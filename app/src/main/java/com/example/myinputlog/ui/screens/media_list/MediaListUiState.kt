package com.example.myinputlog.ui.screens.media_list

import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.models.LabelUiModel

sealed interface MediaListUiState {
    data object Loading : MediaListUiState
    data object Empty : MediaListUiState
    data object Error : MediaListUiState
    data object NetworkError : MediaListUiState
    data class Success(
        val currentCourseId: String = "",
        val allLabels: Set<LabelUiModel> = emptySet(),
        val allCountries: Set<CountryUiModel> = emptySet(),
        val filters: MediaFilters = MediaFilters(),
        val appliedSort: SortOptions = SortOptions.DEFAULT
    ) : MediaListUiState
}

data class MediaFilters(
    val searchQuery: String = "",
    val selectedChannels: Set<String> = emptySet(),
    val selectedLabels: Set<String> = emptySet(),
    val selectedCountries: Set<String> = emptySet(),
    val allChannelsSelected: Boolean = false,
    val allLabelsSelected: Boolean = false,
    val unassignedLabelSelected: Boolean = false,
    val allCountriesSelected: Boolean = false,
    val unassignedCountrySelected: Boolean = false
) {
    fun hasLabelFilter(): Boolean = selectedLabels.isNotEmpty() || unassignedLabelSelected

    fun hasCountryFilter(): Boolean = selectedCountries.isNotEmpty() || unassignedCountrySelected

    fun hasActiveFilters(isChannel: Boolean = false): Boolean {
        val common = searchQuery.isNotEmpty() || hasCountryFilter() || hasLabelFilter()
        return if (isChannel) common else common || selectedChannels.isNotEmpty()
    }
}