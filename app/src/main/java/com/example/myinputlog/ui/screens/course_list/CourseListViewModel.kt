package com.example.myinputlog.ui.screens.course_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.utils.StringProvider
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.models.toCourseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val repository: StorageDataRepository, private val stringProvider: StringProvider
) : ViewModel() {
    val courseListUiState: StateFlow<CourseListUiState> =
        combine(repository.courses, repository.currentCourseId) { courses, currentId ->
            when {
                courses.isEmpty() -> CourseListUiState.Empty

                else -> {
                    val courseList = courses.map { it.toCourseUiModel(stringProvider) }
                    val selectedCourse = courseList.firstOrNull { it.id == currentId }
                    CourseListUiState.Success(courseList, selectedCourse)
                }
            }
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CourseListUiState.Loading
        )

    fun changeCurrentCourseId(newCourse: CourseUiModel) {
        viewModelScope.launch {
            repository.setCurrentCourse(newCourse.id)
        }
    }
}