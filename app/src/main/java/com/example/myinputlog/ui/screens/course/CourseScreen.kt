package com.example.myinputlog.ui.screens.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.screens.utils.IME_ACTION_DONE
import com.example.myinputlog.ui.screens.utils.IME_ACTION_NEXT
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.theme.spacing

object CourseDestination : NavigationDestination {
    override val route: String = "course"
    override val titleRes: Int = R.string.course_screen_title
    const val courseIdArg = "courseId"
    val routeWithArgs = "$route/{$courseIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    modifier: Modifier = Modifier,
    courseViewModel: CourseViewModel,
    onNavigateUp: () -> Unit
) {
    val courseUiState = courseViewModel.courseUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MyInputLogTopAppBar(
                title = "",
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                hasDeleteAction = courseUiState.value.id.isNotBlank(),
                hasSaveAction = true,
                isFormValid = courseUiState.value.isFormValid,
                onDelete = { courseViewModel.toggleDialogVisibility(true) },
                onSave = {
                    courseViewModel.persistCourse()
                    onNavigateUp()
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (courseUiState.value.isLoading) {
            LoadingBox()
        } else {
            CourseEditBody(
                modifier = Modifier.padding(innerPadding),
                courseUiState = courseUiState.value,
                onCourseValueChange = courseViewModel::updateUiState,
                onDone = {
                    courseViewModel.persistCourse()
                    onNavigateUp()
                }
            )
        }
    }
    if (courseUiState.value.isDialogVisible) {
        ConfirmDeleteCourseDialog(
            courseName = courseUiState.value.name,
            onConfirm = {
                courseViewModel.deleteCourse()
                onNavigateUp()
            },
            onDismiss = {
                courseViewModel.toggleDialogVisibility(false)
            }
        )
    }
}

@Composable
fun CourseEditBody(
    modifier: Modifier = Modifier,
    courseUiState: CourseUiState,
    onCourseValueChange: (CourseUiState) -> Unit,
    onDone: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        OutlinedTextField(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.small
                )
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.course_name_label)) },
            value = courseUiState.name,
            onValueChange = { name: String ->
                onCourseValueChange(
                    courseUiState.copy(
                        name = name
                    )
                )
            },
            singleLine = true,
            keyboardOptions = IME_ACTION_NEXT,
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        OutlinedTextField(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.small
                )
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.course_goal_label)) },
            value = courseUiState.goalInHours,
            onValueChange = { goal ->
                onCourseValueChange(
                    courseUiState.copy(
                        goalInHours = goal
                    )
                )
            },
            singleLine = true,
            keyboardOptions = IME_ACTION_NEXT.copy(
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        OutlinedTextField(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.small
                )
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.course_other_source_hours_label)) },
            value = courseUiState.otherSourceHours,
            onValueChange = { otherSourceHours ->
                onCourseValueChange(
                    courseUiState.copy(
                        otherSourceHours = otherSourceHours
                    )
                )
            },
            singleLine = true,
            keyboardOptions = IME_ACTION_DONE.copy(
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone()
                    focusManager.clearFocus()
                }
            )
        )
    }
}

@Composable
private fun ConfirmDeleteCourseDialog(
    modifier: Modifier = Modifier,
    courseName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_dialog_title, courseName)) },
        text = { Text(stringResource(R.string.delete_course_phrase, courseName)) },
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
                Text(text = stringResource(R.string.confirm_delete))
            }
        }
    )
}