package com.example.myinputlog.ui.screens.ui_settings

import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme

sealed interface UiSettingsUiState {
    data object Loading : UiSettingsUiState
    data object Error : UiSettingsUiState
    data class Success(
        val selectedConfettiVariant: ConfettiOptions = ConfettiOptions.OPTION1,
        val selectedMode: AppTheme = AppTheme.SYSTEM
    ) : UiSettingsUiState
}