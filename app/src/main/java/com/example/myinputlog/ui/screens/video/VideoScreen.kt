package com.example.myinputlog.ui.screens.video

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox

object VideoDestination : NavigationDestination {
    override val route: String = "video"
    override val titleRes: Int = R.string.video_screen_title
    const val videoIdArg = "videoId"
    val routeWithArgs = "$route/{$videoIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    videoViewModel: VideoViewModel,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val videoUiState = videoViewModel.videoUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MyInputLogTopAppBar(
                title = "",
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (videoUiState.value.isLoading) {
            LoadingBox()
        } else {
            Text(text = "VIDEO", Modifier.padding(innerPadding))
        }
    }
}