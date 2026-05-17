package com.example.myinputlog.ui.screens.landing

import androidx.lifecycle.ViewModel
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.ui.navigation.HomeRoute
import com.example.myinputlog.ui.navigation.LoginRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    fun onAppStart(navigateWithPopUp: (Any) -> Unit) {
        if (accountService.currentUserId.isNotBlank()) {
            navigateWithPopUp(HomeRoute)
        } else {
            navigateWithPopUp(LoginRoute)
        }
    }
}