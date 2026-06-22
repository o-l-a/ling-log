package com.example.myinputlog.ui.screens.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.utils.StringProvider
import com.example.myinputlog.ui.models.CountryGroupUiModel
import com.example.myinputlog.ui.models.toUiModel
import com.example.myinputlog.ui.navigation.CourseRoute
import com.example.myinputlog.ui.navigation.DEFAULT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storageDataRepository: StorageDataRepository,
    private val stringProvider: StringProvider
) : ViewModel() {
    sealed class CourseUiEvent {
        object NavigateBack : CourseUiEvent()
    }

    private val courseRoute = savedStateHandle.toRoute<CourseRoute>()
    private val courseId: String = sanitizeInitialCourseId(courseRoute.courseId)
    private val _fields = MutableStateFlow(CourseFields())
    private val _isLoading = MutableStateFlow(true)
    private val _isDialogVisible = MutableStateFlow(false)

    private val _uiEvent = Channel<CourseUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val courseUiState: StateFlow<CourseUiState> = combine(
        _fields, _isLoading, _isDialogVisible, storageDataRepository.countryGroups
    ) { fields, loading, dialogVisible, countryGroups ->
        if (loading) {
            CourseUiState.Loading
        } else {
            CourseUiState.Success(
                courseFields = fields,
                isFormValid = validateFields(fields),
                isDialogVisible = dialogVisible,
                allCountryGroups = countryGroups.map { it.toUiModel(stringProvider) })
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
            if (courseId.isNotBlank()) {
                val course = storageDataRepository.getUserCourse(courseId)
                if (course != null) {
                    _fields.value = CourseFields(
                        name = course.course.name,
                        goalInHours = course.course.goalInHours.toString(),
                        otherSourceHours = course.course.otherSourceHours.toString(),
                        countryGroup = course.countryGroup.toUiModel(stringProvider)
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

    fun updateCountryGroup(countryGroup: CountryGroupUiModel) {
        _fields.update { it.copy(countryGroup = countryGroup) }
    }

    fun toggleDialogVisibility(visible: Boolean) {
        _isDialogVisible.value = visible
    }

    private fun validateFields(fields: CourseFields): Boolean {
        val isNameValid = fields.name.isNotBlank()
        val isGoalValid = fields.goalInHours.toDoubleOrNull() != null
        val isOtherHoursValid = fields.otherSourceHours.toDoubleOrNull() != null
        val isCountryGroupValid = fields.countryGroup != null
        return isNameValid && isGoalValid && isOtherHoursValid && isCountryGroupValid
    }

    fun deleteCourse() {
        toggleDialogVisibility(false)
        viewModelScope.launch {
            storageDataRepository.deleteUserCourse(courseId)
            _uiEvent.send(CourseUiEvent.NavigateBack)
        }
    }

    fun saveCourse() {
        viewModelScope.launch {
            val currentFields = _fields.value
            if (validateFields(currentFields) && currentFields.countryGroup != null) {
                val course = currentFields.toUserCourse(id = courseId)
                val courseEntity = course.toCourseEntity()
                storageDataRepository.saveUserCourse(courseEntity)
                _uiEvent.send(CourseUiEvent.NavigateBack)
            }
        }
    }

    private fun sanitizeInitialCourseId(id: String): String {
        return if (id == DEFAULT_ID.toString()) {
            ""
        } else {
            id
        }
    }
}