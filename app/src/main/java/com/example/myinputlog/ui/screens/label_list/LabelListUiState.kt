package com.example.myinputlog.ui.screens.label_list

import com.example.myinputlog.data.model.UserLabel

sealed interface LabelListUiState {
    data object Loading : LabelListUiState
    data object Empty : LabelListUiState
    data object Error : LabelListUiState
    data class Success(val userLabels: List<UserLabel> = listOf()) : LabelListUiState
}