package com.example.myinputlog.ui.screens.home

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen

object HomeDestination : NavigationDestination {
    override val route: String = "home"
    override val titleRes: Int = R.string.home_screen_title
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: () -> Unit,
    navigateToYouTubeVideo: (String) -> Unit
) {
    val homeUiState = homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val userCourses = homeUiState.value.userCourses.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = navigateToYouTubeVideoEntry
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            }
        },
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Home,
                onBottomNavClicked = onBottomNavClicked
            )
        }
    ) { innerPadding ->
        Text(
            text = "HOME",
            modifier = modifier.padding(innerPadding)
        )
    }
}