package com.example.myinputlog.ui.screens.label_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.toLabelUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LabelListViewModel @Inject constructor(repository: StorageDataRepository) : ViewModel() {
    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    val labelListUiState: StateFlow<LabelListUiState> = repository.getLabelsFlow().map { labels ->
        when {
            labels.isEmpty() -> LabelListUiState.Empty

            else -> {
                LabelListUiState.Success(labels.map { it.toLabelUiModel() })
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LabelListUiState.Loading
    )
}