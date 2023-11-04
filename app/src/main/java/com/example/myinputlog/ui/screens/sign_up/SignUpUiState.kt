package com.example.myinputlog.ui.screens.sign_up

import androidx.annotation.StringRes
import com.example.myinputlog.R

data class SignUpUiState(
    val username: String = "",
    val password: String = "",
    val email: String = "",

    val isFormValid: Boolean = false,
    val isUsernameValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isEmailValid: Boolean = true,

    @StringRes val emailErrorMessage: Int = R.string.email_invalid,
    @StringRes val generalErrorMessage: Int = R.string.something_went_wrong,

    val isPasswordVisible: Boolean = false,
    val isGeneralErrorVisible: Boolean = false
)