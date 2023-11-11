package com.example.myinputlog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService
) : ViewModel() {
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()
    private val userCourses = storageService.userCourses

    init {
        viewModelScope.launch {
            val currentCourseId = preferenceStorageService.currentCourseId.firstOrNull() ?: ""
            val currentCourse = userCourses.firstOrNull()?.find {
                it.id == currentCourseId
            } ?: UserCourse()
            _homeUiState.update {
                currentCourse.toHomeUiState().copy(
                    userCourses = userCourses,
                    isLoading = false,
                )
            }
            try {
                val courseStatistics = storageService.getCourseStatistics(currentCourseId)
                _homeUiState.update {
                    it.copy(
                        courseStatistics = courseStatistics,
                        networkError = false
                    )
                }
            } catch (e: Exception) {
                _homeUiState.update {
                    it.copy(networkError = true)
                }
            }
        }
    }

    fun changeCurrentCourseId(newCourse: UserCourse) {
        viewModelScope.launch {
            preferenceStorageService.saveCurrentCourseId(newCourse.id)
            val currentCourse = userCourses.firstOrNull()?.find {
                it.id == (preferenceStorageService.currentCourseId.firstOrNull() ?: "")
            } ?: UserCourse()
            _homeUiState.update {
                currentCourse.toHomeUiState().copy(
                    userCourses = userCourses,
                )
            }
            try {
                val courseStatistics = storageService.getCourseStatistics(currentCourse.id)
                _homeUiState.update {
                    it.copy(
                        courseStatistics = courseStatistics,
                        networkError = false
                    )
                }
            } catch (e: Exception) {
                _homeUiState.update {
                    it.copy(networkError = true)
                }
            }
        }
    }
}