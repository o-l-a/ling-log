package com.example.myinputlog.ui.screens.course_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.utils.StringProvider
import com.example.myinputlog.ui.models.toCourseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    repository: StorageDataRepository, private val stringProvider: StringProvider
) : ViewModel() {
    val courseListUiState: StateFlow<CourseListUiState> = repository.courses.map { courses ->
        when {
            courses.isEmpty() -> CourseListUiState.Empty

            else -> {
                CourseListUiState.Success(courses.map { it.toCourseUiModel(stringProvider) })
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CourseListUiState.Loading
    )
}