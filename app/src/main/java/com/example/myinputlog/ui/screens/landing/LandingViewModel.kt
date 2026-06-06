package com.example.myinputlog.ui.screens.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.ui.navigation.LoginRoute
import com.example.myinputlog.ui.navigation.MediaListRoute
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val accountService: Lazy<AccountService>
) : ViewModel() {

    fun onAppStart(navigateWithPopUp: (Any) -> Unit) {
        viewModelScope.launch {
            val userId = accountService.get().currentUserId
            if (userId.isNotBlank()) {
                // TODO: change to HomeRoute
                navigateWithPopUp(MediaListRoute)
            } else {
                navigateWithPopUp(LoginRoute)
            }
        }
    }
}