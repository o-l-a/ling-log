package com.example.myinputlog.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.R
import com.example.myinputlog.data.service.AccountService
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {
    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState = _loginUiState.asStateFlow()

    fun updateEmail(updatedEmail: String) {
        _loginUiState.update {
            it.copy(
                email = updatedEmail.trim()
            )
        }
    }

    fun updatePassword(updatedPassword: String) {
        _loginUiState.update {
            it.copy(
                password = updatedPassword
            )
        }
    }

    fun togglePasswordVisibility(visible: Boolean) {
        _loginUiState.update {
            it.copy(
                isPasswordVisible = visible
            )
        }
    }

    private fun clearErrorMessage() {
        _loginUiState.update {
            it.copy(
                isGeneralErrorVisible = false
            )
        }
    }

    fun onLoginClick(onLoginClick: () -> Unit) {
        val email = loginUiState.value.email
        val password = loginUiState.value.password
        clearErrorMessage()
        viewModelScope.launch {
            try {
                accountService.signIn(email, password)
                onLoginClick()
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> {
                        _loginUiState.update {
                            it.copy(
                                isFormValid = false
                            )
                        }
                    }

                    is FirebaseNetworkException -> {
                        _loginUiState.update {
                            it.copy(
                                generalErrorMessage = R.string.network_error,
                                isGeneralErrorVisible = true
                            )
                        }
                    }

                    else -> {
                        _loginUiState.update {
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
