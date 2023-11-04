package com.example.myinputlog.ui.screens.login

import androidx.annotation.StringRes
import com.example.myinputlog.R

data class LoginUiState(
    val password: String = "",
    val email: String = "",

    val isFormValid: Boolean = true,

    @StringRes val generalErrorMessage: Int = R.string.something_went_wrong,

    val isPasswordVisible: Boolean = false,
    val isGeneralErrorVisible: Boolean = false
)