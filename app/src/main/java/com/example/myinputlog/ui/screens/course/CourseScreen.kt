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
import androidx.compose.runtime.getValue
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
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.theme.spacing

object CourseDestination : NavigationDestination {
    override val route: String = "course"
    override val titleRes: Int = R.string.course_screen_title
    const val COURSE_ID_ARG = "courseId"
    val routeWithArgs = "$route/{$COURSE_ID_ARG}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    modifier: Modifier = Modifier, courseViewModel: CourseViewModel, onNavigateUp: () -> Unit
) {
    val courseUiState by courseViewModel.courseUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            if (courseUiState is CourseUiState.Success) {
                MyInputLogTopAppBar(
                    title = "",
                    canNavigateBack = true,
                    navigateUp = onNavigateUp,
                    hasDeleteAction = (courseUiState as CourseUiState.Success).courseFields.id.isNotBlank(),
                    hasSaveAction = true,
                    isFormValid = (courseUiState as CourseUiState.Success).isFormValid,
                    onDelete = { courseViewModel.toggleDialogVisibility(true) },
                    onSave = {
                        courseViewModel.persistCourse()
                        onNavigateUp()
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        }) { innerPadding ->
        when (courseUiState) {
            is CourseUiState.Loading -> {
                LoadingBox()
            }

            is CourseUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            is CourseUiState.Success -> {
                CourseEditBody(
                    modifier = Modifier.padding(innerPadding),
                    courseUiState = courseUiState as CourseUiState.Success,
                    onNameChange = courseViewModel::updateName,
                    onGoalChange = courseViewModel::updateGoal,
                    onOtherHoursChange = courseViewModel::updateOtherHours,
                    onDone = {
                        courseViewModel.persistCourse()
                        onNavigateUp()
                    })

                if ((courseUiState as CourseUiState.Success).isDialogVisible) {
                    ConfirmDeleteCourseDialog(
                        courseName = (courseUiState as CourseUiState.Success).courseFields.name,
                        onConfirm = {
                            courseViewModel.deleteCourse()
                            onNavigateUp()
                        },
                        onDismiss = {
                            courseViewModel.toggleDialogVisibility(false)
                        })
                }
            }
        }
    }
}

@Composable
fun CourseEditBody(
    modifier: Modifier = Modifier,
    courseUiState: CourseUiState.Success,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onOtherHoursChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxSize(),
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
            value = courseUiState.courseFields.name,
            onValueChange = onNameChange,
            singleLine = true,
            keyboardOptions = IME_ACTION_NEXT,
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) })
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
            value = courseUiState.courseFields.goalInHours,
            onValueChange = onGoalChange,
            singleLine = true,
            keyboardOptions = IME_ACTION_NEXT.copy(
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) })
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
            value = courseUiState.courseFields.otherSourceHours,
            onValueChange = onOtherHoursChange,
            singleLine = true,
            keyboardOptions = IME_ACTION_DONE.copy(
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone()
                    focusManager.clearFocus()
                })
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
        })
}