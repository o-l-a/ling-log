package com.example.myinputlog.ui.screens.ui_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UiSettingsViewModel @Inject constructor(
    val storageDataRepository: StorageDataRepository
) : ViewModel() {

    private val isParty = MutableStateFlow(false)

    val uiSettingsUiState: StateFlow<UiSettingsUiState> = combine(
        storageDataRepository.themeMode, storageDataRepository.confettiColors, isParty
    ) { theme, confetti, party ->
        UiSettingsUiState.Success(
            selectedMode = theme, selectedConfettiVariant = confetti, isParty = party
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiSettingsUiState.Loading
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            storageDataRepository.saveThemeMode(theme)
        }
    }

    fun setConfetti(confetti: ConfettiOptions) {
        viewModelScope.launch {
            storageDataRepository.saveConfettiColors(confetti)
            isParty.value = true
        }
    }

    fun confettiStop() {
        isParty.value = false
    }
}