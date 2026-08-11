package com.example.myinputlog.ui.screens.account

import java.io.File


sealed interface AccountUiState {
    data object Loading : AccountUiState
    data object Error : AccountUiState
    data class Success(
        val username: String = "",
        val newUsername: String = username,
        val email: String = "",
        val imagePath: File? = null,
        val isFormValid: Boolean = false,
        val isDialogVisible: Boolean = false,
        val hideEmail: Boolean = false
    ) : AccountUiState
}