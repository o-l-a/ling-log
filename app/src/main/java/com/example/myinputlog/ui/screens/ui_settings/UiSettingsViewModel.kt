package com.example.myinputlog.ui.screens.ui_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.screens.common.ConfettiOptions
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
    val repository: StorageDataRepository
) : ViewModel() {

    private val isParty = MutableStateFlow(false)

    val uiSettingsUiState: StateFlow<UiSettingsUiState> = combine(
        repository.themeMode,
        repository.confettiColors,
        repository.channelSortDefault,
        repository.videoSortDefault,
        isParty
    ) { theme, confetti, cSort, vSort, party ->
        UiSettingsUiState.Success(
            selectedMode = theme,
            selectedConfettiVariant = confetti,
            selectedChannelSort = cSort,
            selectedVideoSort = vSort,
            isParty = party
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiSettingsUiState.Loading
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            repository.saveThemeMode(theme)
        }
    }

    fun setConfetti(confetti: ConfettiOptions) {
        viewModelScope.launch {
            repository.saveConfettiColors(confetti)
            isParty.value = true
        }
    }

    fun setChannelSort(sort: SortOptions) {
        viewModelScope.launch {
            repository.saveChannelSortDefault(sort)
        }
    }

    fun setVideoSort(sort: SortOptions) {
        viewModelScope.launch {
            repository.saveVideoSortDefault(sort)
        }
    }

    fun confettiStop() {
        isParty.value = false
    }
}