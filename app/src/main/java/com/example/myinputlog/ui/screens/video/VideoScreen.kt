package com.example.myinputlog.ui.screens.video

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomSaveBar
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.theme.spacing
import java.util.Date

object VideoDestination : NavigationDestination {
    override val route: String = "video"
    override val titleRes: Int = R.string.video_screen_title
    const val videoIdArg = "videoId"
    const val courseIdArg = "courseId"
    val routeWithArgs = "$route/{$courseIdArg}/{$videoIdArg}"
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
    val datePickerState = rememberDatePickerState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MyInputLogTopAppBar(
                title = "",
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                hasDeleteAction = videoUiState.value.id.isNotBlank(),
                onDelete = { videoViewModel.toggleDialogVisibility(true) },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            MyInputLogBottomSaveBar(
                onCancelClicked = onNavigateUp,
                onSaveClicked = {
                    videoViewModel.persistVideo()
                    onNavigateUp()
                },
                isFormValid = videoUiState.value.isFormValid
            )
        }
    ) { innerPadding ->
        if (videoUiState.value.isLoading) {
            LoadingBox()
        } else {
            VideoEditBody(
                modifier = Modifier.padding(innerPadding),
                videoUiState = videoUiState.value,
                onCourseValueChange = videoViewModel::updateUiState
            )
        }
    }
    if (videoUiState.value.isDeleteDialogVisible) {
        ConfirmDeleteVideoDialog(
            videoName = videoUiState.value.title,
            onConfirm = {
                videoViewModel.deleteVideo()
                onNavigateUp()
            },
            onDismiss = {
                videoViewModel.toggleDialogVisibility(false)
            }
        )
    }
    if (videoUiState.value.isDatePickerDialogVisible) {
        MyDatePickerDialog(
            onConfirm = {
                videoViewModel.updateUiState(
                    videoUiState.value.copy(
                        watchedOn = Date(datePickerState.selectedDateMillis!!),
                        isDatePickerDialogVisible = false
                    )
                )
            },
            onDismiss = {
                videoViewModel.updateUiState(
                    videoUiState.value.copy(isDatePickerDialogVisible = false)
                )
            },
            datePickerState = datePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditBody(
    modifier: Modifier = Modifier,
    videoUiState: VideoUiState,
    onCourseValueChange: (VideoUiState) -> Unit,
) {
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
            label = { Text(stringResource(R.string.video_link_label)) },
            value = videoUiState.link,
            onValueChange = { link: String ->
                onCourseValueChange(
                    videoUiState.copy(
                        link = link
                    )
                )
            },
            singleLine = true
        )
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            repeat(3) { index ->
                InputChip(
                    selected = false,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall),
                    onClick = { /* do something*/ },
                    label = { Text("Chip $index") }
                )
            }
        }
    }
}

@Composable
fun ConfirmDeleteVideoDialog(
    modifier: Modifier = Modifier,
    videoName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_dialog_title, videoName)) },
        text = { Text(stringResource(R.string.delete_video_phrase, videoName)) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.ok_text))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_text))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
