package com.example.myinputlog.ui.screens.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService,
    accountService: AccountService
) : ViewModel() {
    private val courseId: String = checkNotNull(savedStateHandle[CourseDestination.COURSE_ID_ARG])
    private val userIdFlow = accountService.currentUser.map { it.id }
    private val _fields = MutableStateFlow(CourseFields())
    private val _isLoading = MutableStateFlow(true)
    private val _isDialogVisible = MutableStateFlow(false)

    val courseUiState: StateFlow<CourseUiState> = combine(
        _fields, _isLoading, _isDialogVisible
    ) { fields, loading, dialogVisible ->
        if (loading) {
            CourseUiState.Loading
        } else {
            CourseUiState.Success(
                courseFields = fields,
                isFormValid = validateFields(fields),
                isDialogVisible = dialogVisible,
                isEdit = fields.id.isNotBlank()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CourseUiState.Loading
    )

    init {
        loadCourse()
    }

    private fun loadCourse() {
        viewModelScope.launch {
            val userId = userIdFlow.first()
            if (courseId != DEFAULT_ID.toString()) {
                val course = storageService.getUserCourse(userId, courseId)
                if (course != null) {
                    _fields.value = CourseFields(
                        id = course.id,
                        name = course.name,
                        goalInHours = course.goalInHours.toString(),
                        otherSourceHours = course.otherSourceHours.toString()
                    )
                }
            }
            _isLoading.value = false
        }
    }

    fun updateName(name: String) {
        _fields.update { it.copy(name = name) }
    }

    fun updateGoal(goal: String) {
        _fields.update { it.copy(goalInHours = goal) }
    }

    fun updateOtherHours(hours: String) {
        _fields.update { it.copy(otherSourceHours = hours) }
    }

    fun toggleDialogVisibility(visible: Boolean) {
        _isDialogVisible.value = visible
    }

    private fun validateFields(fields: CourseFields): Boolean {
        val isNameValid = fields.name.isNotBlank()
        val isGoalValid = fields.goalInHours.toDoubleOrNull() != null
        val isOtherHoursValid = fields.otherSourceHours.toDoubleOrNull() != null
        return isNameValid && isGoalValid && isOtherHoursValid
    }

    fun deleteCourse() {
        toggleDialogVisibility(false)
        viewModelScope.launch {
            val userId = userIdFlow.first()
            storageService.deleteUserCourse(userId, courseId)
            val currentCourseId = preferenceStorageService.currentCourseId.firstOrNull() ?: ""
            if (currentCourseId == courseId) {
                val firstAvailable =
                    storageService.getUserCourses(userId).firstOrNull()?.getOrNull(0)
                firstAvailable?.let { preferenceStorageService.saveCurrentCourseId(it.id) }
            }
        }
    }

    fun persistCourse() {
        viewModelScope.launch {
            val currentFields = _fields.value
            val userId = userIdFlow.first()
            val course = currentFields.toUserCourse()
            if (course.id.isBlank()) {
                val newCourseId = storageService.saveUserCourse(userId, course)
                preferenceStorageService.saveCurrentCourseId(newCourseId)
            } else {
                storageService.updateUserCourse(userId, course)
            }
        }
    }
}