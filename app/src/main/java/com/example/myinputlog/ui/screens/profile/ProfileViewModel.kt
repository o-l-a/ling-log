package com.example.myinputlog.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.impl.DefaultAccountService
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            accountService.currentUser.collect { userData ->
                _profileUiState.update {
                    it.copy(
                        currentCourseId = currentCourseId,
                        username = userData.username,
                        email = userData.email,
                        id = userData.id,
                        courses = storageService.userCourses,
                        newUsername = userData.username
                    )
                }
                Log.d("PROFILE", userData.username)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accountService.signOut()
        }
    }

    fun toggleDialogVisibility(visible: Boolean) {
        _profileUiState.update {
            it.copy(
                isConfirmDialogVisible = visible
            )
        }
    }

    fun toggleUsernameDialogVisibility(visible: Boolean) {
        _profileUiState.update {
            it.copy(
                isUsernameDialogVisible = visible
            )
        }
    }

    fun updateUiState(updatedUiState: ProfileUiState) {
        _profileUiState.update {
            updatedUiState
        }
    }

    fun updateUsername(callback: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                accountService.changeUsername(profileUiState.value.newUsername)
                _profileUiState.update {
                    it.copy(username = it.newUsername)
                }
                toggleUsernameDialogVisibility(false)
                callback(0)
            } catch (e: Exception) {
                toggleUsernameDialogVisibility(false)
                callback(1)
            }
        }
    }

    fun deleteAccount(onConfirm: () -> Unit, callback: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                accountService.deleteAccount()
                onConfirm()
                callback(0)
            } catch (e: Exception) {
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    toggleDialogVisibility(false)
                    callback(1)
                } else {
                    toggleDialogVisibility(false)
                    callback(2)
                }
            }
        }
    }

    fun toggleHideEmail(newValue: Boolean = true) {
        _profileUiState.update {
            it.copy(hideEmail = newValue)
        }
    }
}