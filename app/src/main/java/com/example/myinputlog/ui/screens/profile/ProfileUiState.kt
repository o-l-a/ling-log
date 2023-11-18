package com.example.myinputlog.ui.screens.profile

data class ProfileUiState(
    val currentCourseId: String = "",
    val username: String = "",
    val id: String = "",
    val isDialogVisible: Boolean = false,
    val isNetworkError: Boolean = false,
)