package com.example.myinputlog.ui.screens.account

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.R
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.screens.utils.UiText
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AccountViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val storageDataRepository: StorageDataRepository
) : ViewModel() {
    sealed class AccountUiEvent {
        data class ShowSnackbar(val message: UiText) : AccountUiEvent()
        object NavigateWithPopUp : AccountUiEvent()
        object NavigateUp : AccountUiEvent()
    }

    private val _isDialogVisible = MutableStateFlow(false)
    private val _isEmailHidden = MutableStateFlow(false)
    private val _newUsername = MutableStateFlow<String?>(null)
    private val _imagePath = MutableStateFlow<File?>(null)

    private val _uiEvent = Channel<AccountUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val accountUiState: StateFlow<AccountUiState> = combine(
        storageDataRepository.currentUser,
        _isDialogVisible,
        _isEmailHidden,
        _newUsername,
        _imagePath
    ) { user, dialog, email, newName, img ->
        AccountUiState.Success(
            username = newName ?: user.username,
            email = user.email,
            imagePath = img,
            isFormValid = !newName.isNullOrBlank(),
            isDialogVisible = dialog,
            hideEmail = email
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountUiState.Loading
    )

    init {
        viewModelScope.launch {
            storageDataRepository.currentUser.firstOrNull().let { user ->
                if (user != null) {
                    val file = File(context.filesDir, "profile_photo_${user.id}.jpg")
                    if (file.exists()) {
                        _imagePath.value = file
                    }
                }
            }
        }
    }

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
                        _uiEvent.send(AccountUiEvent.NavigateUp)
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

    fun saveProfilePhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            storageDataRepository.currentUser.firstOrNull().let { user ->
                if (user != null) {
                    val file = File(context.filesDir, "profile_photo_${user.id}.jpg")
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            val originalBitmap = BitmapFactory.decodeStream(input)

                            if (originalBitmap != null) {
                                val squareBitmap = cropToSquare(originalBitmap)
                                val finalBitmap = squareBitmap.scale(512, 512)
                                file.outputStream().use { output ->
                                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                                }
                                if (squareBitmap != originalBitmap) squareBitmap.recycle()
                                finalBitmap.recycle()
                                originalBitmap.recycle()
                                _imagePath.value = file
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _uiEvent.send(AccountUiEvent.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
                    }
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

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newSize = if (height > width) width else height

        val left = (width - newSize) / 2
        val top = (height - newSize) / 2

        return Bitmap.createBitmap(bitmap, left, top, newSize, newSize)
    }

    companion object {
        private const val TAG = "AccountViewModel"
    }
}