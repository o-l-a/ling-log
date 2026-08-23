package com.example.myinputlog.ui.screens.ui_settings

import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.ui.screens.common.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme

sealed interface UiSettingsUiState {
    data object Loading : UiSettingsUiState
    data object Error : UiSettingsUiState
    data class Success(
        val selectedConfettiVariant: ConfettiOptions = ConfettiOptions.OPTION1,
        val selectedMode: AppTheme = AppTheme.SYSTEM,
        val selectedChannelSort: SortOptions = SortOptions.DEFAULT,
        val selectedVideoSort: SortOptions = SortOptions.DEFAULT,
        val isParty: Boolean = false
    ) : UiSettingsUiState
}