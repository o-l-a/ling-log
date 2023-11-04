package com.example.myinputlog.ui.screens.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.screens.utils.composable.MyInputLogAppIcon
import kotlinx.coroutines.delay

private const val LANDING_TIMEOUT = 1L

object LandingDestination : NavigationDestination {
    override val route: String = "landing"
    override val titleRes: Int = 0
}

@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    navigateWithPopUp: (String) -> Unit,
    viewModel: LandingViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyInputLogAppIcon(modifier = Modifier.size(90.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )
        LaunchedEffect(true) {
            delay(LANDING_TIMEOUT)
            viewModel.onAppStart(navigateWithPopUp)
        }
    }
}