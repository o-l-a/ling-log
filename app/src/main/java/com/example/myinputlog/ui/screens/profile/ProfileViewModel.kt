package com.example.myinputlog.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.R
import com.example.myinputlog.data.service.impl.DefaultAccountService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
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

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState = _profileUiState.asStateFlow()
    val courses = storageService.userCourses

    init {
        viewModelScope.launch {
            accountService.currentUser.collect { userData ->
                _profileUiState.update {
                    it.copy(username = userData.username, id = userData.id)
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
                isDialogVisible = visible
            )
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
}