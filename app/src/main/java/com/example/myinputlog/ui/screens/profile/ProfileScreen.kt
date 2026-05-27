package com.example.myinputlog.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.navigation.SettingsScreen
import com.example.myinputlog.ui.screens.utils.composable.LeadingIconWithText
import com.example.myinputlog.ui.screens.utils.ext.hideEmail
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
    val profileUiState = profileViewModel.profileUiState.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        ProfileTopAppBar(
            profileName = profileUiState.value.username,
            profileEmail = profileUiState.value.email,
            onHideEmailClicked = profileViewModel::toggleHideEmail,
            hideEmail = profileUiState.value.hideEmail,
            onChangeUsernameClicked = { profileViewModel.toggleUsernameDialogVisibility(true) },
            scrollBehavior = scrollBehavior
        )
    }, bottomBar = {
        MyInputLogBottomNavBar(
            selectedScreen = Screen.Profile,
            onBottomNavClicked = onBottomNavClicked,
            navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(profileUiState.value.currentCourseId) })
    }) { innerPadding ->
        ProfileBody(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            navigationItems = navigationItems
        )
    }
}

@Composable
fun ProfileBody(
    modifier: Modifier = Modifier, navigationItems: Map<SettingsScreen, () -> Unit>
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
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(
            MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall,
        ),
        userScrollEnabled = isScrollEnabled
    ) {
        items(
            items = items, key = { (screen, _) -> screen.route.hashCode() }) { (screen, onClick) ->
            SettingsCard(
                label = stringResource(id = screen.resourceId), onClick = onClick
            )
        }
    }
}


@Composable
private fun SettingsCard(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card (
        onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = 0.6f
            )
        )
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label, style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(
    modifier: Modifier = Modifier,
    profileName: String,
    profileEmail: String,
    onHideEmailClicked: (Boolean) -> Unit,
    hideEmail: Boolean,
    onChangeUsernameClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(modifier = modifier, scrollBehavior = scrollBehavior, navigationIcon = {
        LeadingIconWithText(
            modifier = Modifier
                .padding(start = MaterialTheme.spacing.medium)
                .size(MaterialTheme.spacing.large), name = profileName
        )
    }, title = {
        ListItem(modifier = Modifier.fillMaxWidth(), headlineContent = {
            Text(
                text = profileName, style = MaterialTheme.typography.bodyLarge
            )
        }, supportingContent = {
            Text(
                text = if (hideEmail) profileEmail.hideEmail() else profileEmail,
                style = MaterialTheme.typography.bodyMedium
            )
        })
    }, actions = {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopStart)
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
                DropdownMenuItem(
                    text = {
                    Text(
                        stringResource(
                            if (hideEmail) {
                                R.string.show_email
                            } else {
                                R.string.hide_email
                            }
                        )
                    )
                }, onClick = {
                    expanded = false
                    onHideEmailClicked(!hideEmail)
                }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
                DropdownMenuItem(
                    text = {
                    Text(stringResource(R.string.change_username))
                }, onClick = {
                    expanded = false
                    onChangeUsernameClicked()
                }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    })
}