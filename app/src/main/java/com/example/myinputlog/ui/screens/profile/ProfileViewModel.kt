package com.example.myinputlog.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.impl.DefaultAccountService
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountService: DefaultAccountService,
    storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService
) : ViewModel() {

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState = _profileUiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentCourseId = preferenceStorageService.currentCourseId.firstOrNull() ?: ""
            accountService.currentUser.filter { it.id.isNotBlank() }.collect { userData ->
                _profileUiState.update {
                    it.copy(
                        currentCourseId = currentCourseId,
                        username = userData.username,
                        email = userData.email,
                        id = userData.id,
                        courses = storageService.getUserCourses(accountService.currentUser.first().id),
                        newUsername = userData.username
                    )
                }
                Log.d("PROFILE", userData.username)
            }
        }
    }

    fun toggleUsernameDialogVisibility(visible: Boolean) {
        _profileUiState.update {
            it.copy(
                isUsernameDialogVisible = visible
            )
        }
    }

    fun toggleHideEmail(newValue: Boolean = true) {
        _profileUiState.update {
            it.copy(hideEmail = newValue)
        }
    }
}