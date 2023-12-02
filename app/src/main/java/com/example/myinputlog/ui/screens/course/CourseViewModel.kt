package com.example.myinputlog.ui.screens.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService
) : ViewModel() {
    private val _courseUiState = MutableStateFlow(CourseUiState())
    val courseUiState = _courseUiState.asStateFlow()

    private val courseId: String = checkNotNull(savedStateHandle[CourseDestination.courseIdArg])

    init {
        if (courseId != DEFAULT_ID.toString()) {
            viewModelScope.launch {
                val course = storageService.getUserCourse(courseId)
                if (course != null) {
                    _courseUiState.update {
                        course.toCourseUiState(
                            isLoading = false
                        )
                    }
                } else {
                    // TODO("this should never happen")
                }
            }
        } else {
            _courseUiState.update {
                it.copy(
                    isLoading = false
                )
            }
        }
    }

    fun updateUiState(updatedUiState: CourseUiState) {
        _courseUiState.update {
            updatedUiState.copy()
        }
        validateForm()
    }

    fun toggleDialogVisibility(visible: Boolean) {
        _courseUiState.update {
            it.copy(
                isDialogVisible = visible
            )
        }
    }

    private fun validateForm() {
        _courseUiState.update {
            it.copy(
                isFormValid = it.name.isNotBlank() && it.goalInHours.isNotBlank() && it.otherSourceHours.isNotBlank()
            )
        }
    }

    fun deleteCourse() {
        toggleDialogVisibility(false)
        viewModelScope.launch {
            storageService.deleteUserCourse(courseId)
            val currentCourseId = preferenceStorageService.currentCourseId.firstOrNull() ?: ""
            if (currentCourseId == courseId) {
                val courses = storageService.userCourses.firstOrNull()
                val firstCourse = courses?.getOrNull(0)
                if (firstCourse != null) {
                    preferenceStorageService.saveCurrentCourseId(firstCourse.id)
                }
            }
        }
    }

    fun persistCourse() {
        viewModelScope.launch {
            val course = _courseUiState.value.toUserCourse()
            if (course.id.isBlank()) {
                val newCourseId = storageService.saveUserCourse(course)
                preferenceStorageService.saveCurrentCourseId(newCourseId)
            } else {
                storageService.updateUserCourse(course)
            }
        }
    }
}