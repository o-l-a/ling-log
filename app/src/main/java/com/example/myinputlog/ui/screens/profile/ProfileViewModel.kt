package com.example.myinputlog.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.repository.StorageDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val storageDataRepository: StorageDataRepository
) : ViewModel() {
    private val _imagePath = MutableStateFlow<File?>(null)

    val currentCourseId: StateFlow<String> = storageDataRepository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    val profileUiState: StateFlow<ProfileUiState> = combine(
        currentCourseId, storageDataRepository.currentUser, _imagePath
    ) { courseId, user, img ->
        if (user.id.isBlank()) {
            ProfileUiState.Error
        } else {
            ProfileUiState.Success(
                currentCourseId = courseId,
                username = user.username,
                email = user.email,
                imagePath = img,
                id = user.id
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState.Loading
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
}