package com.example.myinputlog.ui.screens.account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.R
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.screens.utils.UiText
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AccountViewModel @Inject constructor(
    val storageDataRepository: StorageDataRepository
) : ViewModel() {
    sealed class AccountUiEvent {
        data class ShowSnackbar(val message: UiText) : AccountUiEvent()
        object NavigateWithPopUp : AccountUiEvent()
    }

    private val _isDialogVisible = MutableStateFlow(false)
    private val _isEmailHidden = MutableStateFlow(false)
    private val _newUsername = MutableStateFlow<String?>(null)

    private val _uiEvent = Channel<AccountUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val accountUiState: StateFlow<AccountUiState> = combine(
        storageDataRepository.currentUser, _isDialogVisible, _isEmailHidden, _newUsername
    ) { user, dialog, email, newName ->
        AccountUiState.Success(
            username = newName ?: user.username,
            email = user.email,
            isFormValid = !newName.isNullOrBlank(),
            isDialogVisible = dialog,
            hideEmail = email
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountUiState.Loading
    )

    fun toggleDialogVisibility(visible: Boolean) {
        _isDialogVisible.value = visible
    }

    fun updateUsername(newName: String) {
        _newUsername.value = newName
    }

    fun saveUsername() {
        viewModelScope.launch {
            when (val currentState = accountUiState.value) {
                is AccountUiState.Success -> {
                    try {
                        storageDataRepository.changeUsername(currentState.newUsername)
                    } catch (e: Exception) {
                        e.message?.let { Log.d(TAG, it) }
                        _uiEvent.send(AccountUiEvent.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
                    }
                }

                else -> {
                    _uiEvent.send(AccountUiEvent.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            storageDataRepository.signOut()
            _uiEvent.send(AccountUiEvent.NavigateWithPopUp)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "deleting account")
                storageDataRepository.deleteAccount()
                _uiEvent.send(AccountUiEvent.NavigateWithPopUp)
            } catch (e: Exception) {
                Log.d(TAG, "exception", e)
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    toggleDialogVisibility(false)
                    _uiEvent.send(AccountUiEvent.ShowSnackbar(UiText.StringResource(R.string.recent_login_text)))
                } else {
                    toggleDialogVisibility(false)
                    _uiEvent.send(AccountUiEvent.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
                }
            }
        }
    }

    companion object {
        private const val TAG = "AccountViewModel"
    }
}