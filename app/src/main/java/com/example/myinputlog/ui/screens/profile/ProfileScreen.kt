package com.example.myinputlog.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomNavBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch

private const val TAG = "ProfileScreen"

object ProfileDestination : NavigationDestination {
    override val route: String = "settings"
    override val titleRes: Int = R.string.profile_screen_title
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel,
    onBottomNavClicked: (String) -> Unit,
    navigateToUserCourseEntry: () -> Unit,
    navigateToUserCourse: (String) -> Unit,
    navigateWithPopUp: () -> Unit
) {
    val profileUiState = profileViewModel.profileUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val snackbarGeneralErrorMessage = stringResource(R.string.something_went_wrong)
    val snackbarRecentLogErrorMessage = stringResource(R.string.recent_login_text)

    Scaffold(
        bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Profile,
                onBottomNavClicked = onBottomNavClicked
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
            courses = profileViewModel.courses.collectAsStateWithLifecycle(initialValue = listOf()).value,
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
        modifier = modifier.padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        item {
            Text(
                text = profileUiState.value.username,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        items(courses) { course ->
            CourseContainer(
                course = course,
                onCourseClicked = onCourseClicked
            )
        }
        item {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAddCourseClicked() }
            ) {
                Text(
                    text = stringResource(R.string.new_course_button)
                )
            }
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
    ElevatedCard(
        modifier
            .fillMaxWidth()
            .clickable { onCourseClicked(course.id) }
    ) {
        Column(
            modifier.padding(MaterialTheme.spacing.small)
        ) {
            Text(course.name)
            Text(stringResource(R.string.course_goal_label) + ": " + course.goalInHours.toString())
            Text(stringResource(R.string.course_other_source_hours_label) + ": " + course.otherSourceHours.toString())
        }
    }
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