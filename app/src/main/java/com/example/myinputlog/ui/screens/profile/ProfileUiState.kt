package com.example.myinputlog.ui.screens.profile

import java.io.File

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object Error : ProfileUiState
    data class Success(
        val currentCourseId: String = "",
        val username: String = "",
        val email: String = "",
        val imagePath: File? = null,
        val id: String = "",
    ) : ProfileUiState
}