package com.example.myinputlog.ui.screens.sign_up

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.R
import com.example.myinputlog.data.service.impl.DefaultAccountService
import com.example.myinputlog.ui.screens.utils.ext.isValidEmail
import com.example.myinputlog.ui.screens.utils.ext.isValidPassword
import com.example.myinputlog.ui.screens.utils.ext.isValidUsername
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val accountService: DefaultAccountService
) : ViewModel() {
    private val _signUpUiState = MutableStateFlow(SignUpUiState())
    val signUpUiState = _signUpUiState.asStateFlow()

    fun updateUsername(updatedUsername: String) {
        _signUpUiState.update {
            it.copy(
                username = updatedUsername
            )
        }
        validateUsername()
    }

    fun updateEmail(updatedEmail: String) {
        _signUpUiState.update {
            it.copy(
                email = updatedEmail.trim()
            )
        }
        validateEmail()
    }

    fun updatePassword(updatedPassword: String) {
        _signUpUiState.update {
            it.copy(
                password = updatedPassword
            )
        }
        validatePassword()
    }

    fun togglePasswordVisibility(visible: Boolean) {
        _signUpUiState.update {
            it.copy(
                isPasswordVisible = visible
            )
        }
    }

    private fun validateUsername() {
        _signUpUiState.update {
            it.copy(
                isUsernameValid = it.username.isValidUsername()
            )
        }
    }

    private fun validatePassword() {
        _signUpUiState.update {
            it.copy(
                isPasswordValid = it.password.isValidPassword()
            )
        }
    }

    private fun validateEmail() {
        _signUpUiState.update {
            it.copy(
                isEmailValid = it.email.isValidEmail(),
                emailErrorMessage = R.string.email_invalid
            )
        }
    }

    private fun validateForm() {
        _signUpUiState.update {
            val isEmailValid = it.email.isValidEmail()
            val isPasswordValid = it.password.isValidPassword()
            val isUsernameValid = it.username.isValidUsername()
            val isFormValid = isEmailValid && isPasswordValid && isUsernameValid
            it.copy(
                isEmailValid = isEmailValid,
                isPasswordValid = isPasswordValid,
                isUsernameValid = isUsernameValid,
                isFormValid = isFormValid
            )
        }
    }

    private fun clearErrorMessage() {
        _signUpUiState.update {
            it.copy(
                isGeneralErrorVisible = false
            )
        }
    }

    fun onSignUpClick(onSignUpClick: () -> Unit) {
        clearErrorMessage()
        validateForm()
        if (signUpUiState.value.isFormValid) {
            viewModelScope.launch {
                try {
                    accountService.createAccount(
                        signUpUiState.value.email,
                        signUpUiState.value.password,
                        signUpUiState.value.username
                    )
                    onSignUpClick()
                } catch (e: Exception) {
                    when (e) {
                        is FirebaseAuthUserCollisionException -> {
                            _signUpUiState.update {
                                it.copy(
                                    isEmailValid = false,
                                    emailErrorMessage = R.string.email_taken
                                )
                            }
                        }

                        is FirebaseNetworkException -> {
                            _signUpUiState.update {
                                it.copy(
                                    generalErrorMessage = R.string.network_error,
                                    isGeneralErrorVisible = true
                                )
                            }
                        }

                        else -> {
                            _signUpUiState.update {
                                it.copy(
                                    generalErrorMessage = R.string.something_went_wrong,
                                    isGeneralErrorVisible = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}