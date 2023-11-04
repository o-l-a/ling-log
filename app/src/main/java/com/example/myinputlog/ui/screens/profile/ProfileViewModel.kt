package com.example.myinputlog.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.impl.DefaultAccountService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountService: DefaultAccountService,
    private val storageService: DefaultStorageService
) : ViewModel() {

    private val _settingsUiState = MutableStateFlow(ProfileUiState())
    val settingsUiState = _settingsUiState.asStateFlow()
    val courses = storageService.userCourses

    fun signOut() {
        viewModelScope.launch {
            accountService.signOut()
        }
    }

    fun toggleDialogVisibility(visible: Boolean) {
        _settingsUiState.update {
            it.copy(
                isDialogVisible = visible
            )
        }
    }

    fun deleteAccount(onConfirm: () -> Unit, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                accountService.deleteAccount()
                onConfirm()
                callback(true)
            } catch (e: Exception) {
                toggleDialogVisibility(false)
                callback(false)
            }
        }
    }
}