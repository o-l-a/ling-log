package com.example.myinputlog.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.myinputlog.data.service.impl.DefaultStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storageService: DefaultStorageService
) : ViewModel() {
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()
    val userCourses = storageService.userCourses

    init {
        _homeUiState.update {
            it.copy(
                isLoading = false
            )
        }
    }
}