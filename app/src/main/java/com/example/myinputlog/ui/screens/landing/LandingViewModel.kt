package com.example.myinputlog.ui.screens.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.ui.navigation.HomeRoute
import com.example.myinputlog.ui.navigation.LoginRoute
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val accountService: Lazy<AccountService>
) : ViewModel() {
    private var hasStarted = false

    fun onAppStart(navigateWithPopUp: (Any) -> Unit) {
        if (hasStarted) return
        hasStarted = true

        viewModelScope.launch {
            val userId = accountService.get().currentUserId
            if (userId.isNotBlank()) {
                navigateWithPopUp(HomeRoute)
            } else {
                navigateWithPopUp(LoginRoute)
            }
        }
    }
}