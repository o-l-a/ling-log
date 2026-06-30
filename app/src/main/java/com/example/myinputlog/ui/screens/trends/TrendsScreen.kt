package com.example.myinputlog.ui.screens.trends

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogBottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    modifier: Modifier = Modifier,
    trendsViewModel: TrendsViewModel,
    onBottomNavClicked: (Any) -> Unit,
    navigateToVideoEntry: (String) -> Unit
) {
    val currentCourseId by trendsViewModel.currentCourseId.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        }, bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Trends,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToVideoEntry(currentCourseId) })
        }) { innerPadding ->
            Text("Yeet", modifier = Modifier.padding(innerPadding))
        }
}
