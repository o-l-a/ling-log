package com.example.myinputlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceStorageService: PreferenceStorageService
) : ViewModel() {

    val themeState: StateFlow<AppTheme> = preferenceStorageService.themeMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )
}