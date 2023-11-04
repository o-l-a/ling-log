package com.example.myinputlog.ui.screens.landing

import androidx.lifecycle.ViewModel
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.ui.screens.home.HomeDestination
import com.example.myinputlog.ui.screens.login.LoginDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    fun onAppStart(navigateWithPopUp: (String) -> Unit) {
        if (accountService.currentUserId.isNotBlank()) {
            navigateWithPopUp(HomeDestination.route)
        } else {
            navigateWithPopUp(LoginDestination.route)
        }
    }
}