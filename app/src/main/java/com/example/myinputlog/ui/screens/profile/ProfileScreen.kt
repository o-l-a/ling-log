package com.example.myinputlog.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.screens.utils.IME_ACTION_DONE
import com.example.myinputlog.ui.screens.utils.composable.LeadingIconWithText
import com.example.myinputlog.ui.screens.utils.ext.hideEmail
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch

object ProfileDestination : NavigationDestination {
    override val route: String = "settings"
    override val titleRes: Int = R.string.profile_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToUserCourseEntry: () -> Unit,
    navigateToUserCourse: (String) -> Unit,
    navigateToYouTubeVideoEntry: (String) -> Unit,
    navigateWithPopUp: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val profileUiState = profileViewModel.profileUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val snackbarGeneralErrorMessage = stringResource(R.string.something_went_wrong)
    val snackbarRecentLogErrorMessage = stringResource(R.string.recent_login_text)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ProfileTopAppBar(
                profileName = profileUiState.value.username,
                profileEmail = profileUiState.value.email,
                onHideEmailClicked = profileViewModel::toggleHideEmail,
                hideEmail = profileUiState.value.hideEmail,
                onChangeUsernameClicked = { profileViewModel.toggleUsernameDialogVisibility(true) },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Profile,
                onBottomNavClicked = onBottomNavClicked,
                navigateToYouTubeVideoEntry = { navigateToYouTubeVideoEntry(profileUiState.value.currentCourseId) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (profileUiState.value.isConfirmDialogVisible) {
            ConfirmDeleteAccountDialog(
                onConfirm = {
                    profileViewModel.deleteAccount(navigateWithPopUp) { returnCode ->
                        if (returnCode != 0) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(if (returnCode == 2) snackbarGeneralErrorMessage else snackbarRecentLogErrorMessage)
                            }
                        }
                    }
                },
                onDismiss = {
                    profileViewModel.toggleDialogVisibility(false)
                }
            )
        }
        if (profileUiState.value.isUsernameDialogVisible) {
            EditUsernameDialog(
                profileUiState = profileUiState.value,
                onConfirm = {
                    profileViewModel.updateUsername { returnCode ->
                        if (returnCode != 0) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(snackbarGeneralErrorMessage)
                            }
                        }
                    }
                },
                onDismiss = { profileViewModel.toggleUsernameDialogVisibility(false) },
                onValueChange = { profileViewModel.updateUiState(it) }
            )
        }
        ProfileBody(
            modifier = modifier.padding(innerPadding),
            onSignOutClicked = {
                profileViewModel.signOut()
                navigateWithPopUp()
            },
            onAddCourseClicked = navigateToUserCourseEntry,
            onCourseClicked = navigateToUserCourse,
            courses = profileUiState.value.courses.collectAsStateWithLifecycle(initialValue = listOf()).value,
            toggleDialogVisibility = { visible ->
                profileViewModel.toggleDialogVisibility(visible)
            }
        )
    }
}

@Composable
fun ProfileBody(
    modifier: Modifier = Modifier,
    onSignOutClicked: () -> Unit,
    onAddCourseClicked: () -> Unit,
    onCourseClicked: (String) -> Unit,
    courses: List<UserCourse>,
    toggleDialogVisibility: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(courses) { course ->
            CourseContainer(
                course = course,
                onCourseClicked = onCourseClicked
            )
        }
        item {
            Button(
                onClick = { onAddCourseClicked() }
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(R.string.new_course_button)
                )
            }
        }
        item {
            Divider()
        }
        item {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                onClick = { onSignOutClicked() }
            ) {
                Text(
                    text = stringResource(R.string.log_out)
                )
            }
        }
        item {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                onClick = { toggleDialogVisibility(true) }
            ) {
                Text(
                    text = stringResource(R.string.delete_account)
                )
            }
        }
    }
}

@Composable
private fun CourseContainer(
    modifier: Modifier = Modifier,
    course: UserCourse,
    onCourseClicked: (String) -> Unit
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = MaterialTheme.spacing.small),
        headlineContent = {
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.course_goal_card_text, course.goalInHours),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.course_other_source_hours_card_text,
                    course.otherSourceHours
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            IconButton(onClick = { onCourseClicked(course.id) }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.edit_text)
                )
            }
        }
    )
}

@Composable
private fun ConfirmDeleteAccountDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_account_dialog_title)) },
        text = { Text(stringResource(R.string.delete_account_phrase)) },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(R.string.dismiss_delete))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(text = stringResource(R.string.confirm_delete_account))
            }
        }
    )
}

@Composable
fun EditUsernameDialog(
    modifier: Modifier = Modifier,
    profileUiState: ProfileUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onValueChange: (ProfileUiState) -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        text = {
            OutlinedTextField(
                value = profileUiState.newUsername,
                onValueChange = { onValueChange(profileUiState.copy(newUsername = it)) },
                label = {
                    Text(stringResource(R.string.new_username))
                },
                keyboardOptions = IME_ACTION_DONE,
                keyboardActions = KeyboardActions(
                    onDone = { onConfirm() }
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.save_text))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dismiss_delete))
            }
        }
    )
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
    TopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            LeadingIconWithText(
                modifier = Modifier
                    .padding(start = MaterialTheme.spacing.medium)
                    .size(MaterialTheme.spacing.large),
                name = profileName
            )
        },
        title = {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = {
                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                supportingContent = {
                    Text(
                        text = if (hideEmail) profileEmail.hideEmail() else profileEmail,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        },
        actions = {
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
                        },
                        onClick = {
                            expanded = false
                            onHideEmailClicked(!hideEmail)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.change_username))
                        },
                        onClick = {
                            expanded = false
                            onChangeUsernameClicked()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    )
}