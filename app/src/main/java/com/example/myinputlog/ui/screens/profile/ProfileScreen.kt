package com.example.myinputlog.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.navigation.SettingsScreen
import com.example.myinputlog.ui.screens.common.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.LeadingIconWithText
import com.example.myinputlog.ui.screens.common.composable.LoadingBox
import com.example.myinputlog.ui.screens.common.composable.SettingsCard
import com.example.myinputlog.ui.screens.common.composable.channel.ProfilePicture
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel,
    onBottomNavClicked: (Any) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
    navigationItems: Map<SettingsScreen, () -> Unit>,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val profileUiState by profileViewModel.profileUiState.collectAsStateWithLifecycle()
    val currentCourseId by profileViewModel.currentCourseId.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), bottomBar = {
        MyInputLogBottomNavBar(
            selectedScreen = Screen.Profile,
            onBottomNavClicked = onBottomNavClicked,
            navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(currentCourseId) })
    }) { innerPadding ->
        when (val currentState = profileUiState) {
            ProfileUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            ProfileUiState.Loading -> {
                LoadingBox()
            }

            is ProfileUiState.Success -> {
                ProfileBody(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    profileUiState = currentState,
                    navigationItems = navigationItems
                )
            }
        }
    }
}

@Composable
fun ProfileBody(
    modifier: Modifier = Modifier,
    profileUiState: ProfileUiState.Success,
    navigationItems: Map<SettingsScreen, () -> Unit>
) {
    val items = remember(navigationItems) { navigationItems.toList() }
    val listState = rememberLazyListState()
    val isScrollEnabled by remember {
        derivedStateOf {
            listState.canScrollForward || listState.canScrollBackward
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(
            MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall,
        ),
        userScrollEnabled = isScrollEnabled
    ) {
        item {
            if (profileUiState.imagePath != null) {
                ProfilePicture(
                    Modifier.size(MaterialTheme.spacing.doubleExtraLarge), profileUiState.imagePath
                )
            } else {
                LeadingIconWithText(
                    modifier = Modifier.size(MaterialTheme.spacing.doubleExtraLarge),
                    name = profileUiState.username
                )
            }
        }
        item {
            Text(
                text = profileUiState.username,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
        items(
            items = items, key = { (screen, _) -> screen.route.hashCode() }) { (screen, onClick) ->
            SettingsCard(
                headlineContent = { Text(stringResource(id = screen.resourceId)) },
                onClick = onClick
            )
        }
    }
}
