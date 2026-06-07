package com.example.myinputlog.ui.screens.label_list

import com.example.myinputlog.ui.models.LabelUiModel

sealed interface LabelListUiState {
    object Loading : LabelListUiState
    object Empty : LabelListUiState
    data class Success(val groupedLabels: Map<String, Set<LabelUiModel>>) : LabelListUiState
    data class Error(val message: String?) : LabelListUiState
}
