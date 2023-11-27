package com.example.myinputlog.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.myinputlog.ui.screens.utils.composable.LeadingIconWithText
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
        ProfileBody(
            modifier = modifier.padding(innerPadding),
            onSignOutClicked = {
                profileViewModel.signOut()
                navigateWithPopUp()
            },
            onAddCourseClicked = navigateToUserCourseEntry,
            onCourseClicked = navigateToUserCourse,
            onConfirm = {
                profileViewModel.deleteAccount(navigateWithPopUp) { returnCode ->
                    if (returnCode != 0) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(if (returnCode == 2) snackbarGeneralErrorMessage else snackbarRecentLogErrorMessage)
                        }
                    }
                }
            },
            profileUiState = profileUiState,
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
    onConfirm: () -> Unit,
    profileUiState: State<ProfileUiState>,
    courses: List<UserCourse>,
    toggleDialogVisibility: (Boolean) -> Unit
) {
    if (profileUiState.value.isDialogVisible) {
        ConfirmDeleteAccountDialog(
            onConfirm = onConfirm,
            onDismiss = { toggleDialogVisibility(false) }
        )
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium)
    ) {
//        item {
//            Row(modifier = Modifier.fillMaxWidth()) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = MaterialTheme.spacing.extraSmall),
//                    horizontalAlignment = Alignment.Start
//                ) {
//                    Text(
//                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.extraSmall),
//                        text = profileUiState.value.username,
//                        style = MaterialTheme.typography.titleLarge
//                    )
//                    Text(
//                        text = profileUiState.value.email,
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }
//
//        }
//        item {
//            Divider(modifier = Modifier.padding(bottom = MaterialTheme.spacing.small))
//        }
        items(courses) { course ->
            CourseContainer(
                course = course,
                onCourseClicked = onCourseClicked
            )
        }
        item {
            Button(
//                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSignOutClicked() }
            ) {
                Text(
                    text = stringResource(R.string.log_out)
                )
            }
        }
        item {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
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
            .fillMaxWidth(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(
    modifier: Modifier = Modifier,
    profileName: String,
    profileEmail: String,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
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
                        text = profileEmail, style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    )
}