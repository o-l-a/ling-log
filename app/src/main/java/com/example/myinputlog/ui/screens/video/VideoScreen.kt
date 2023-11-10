package com.example.myinputlog.ui.screens.video

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogBottomSaveBar
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.screens.utils.Country
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.VideoThumbnail
import com.example.myinputlog.ui.screens.utils.dateFormatter
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
                onDelete = { videoViewModel.toggleDeleteDialogVisibility(true) },
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
                onCourseValueChange = videoViewModel::updateUiState,
                onDateChipClicked = { videoViewModel.toggleDatePickerDialogVisibility(true) },
                onDateClearClicked = {
                    videoViewModel.updateUiState(
                        videoUiState.value.copy(
                            watchedOn = null
                        )
                    )
                },
                onLanguageValueChange = {
                    videoViewModel.updateUiState(
                        videoUiState.value.copy(
                            speakersNationality = it
                        )
                    )
                },
                onLanguageClearClicked = {
                    videoViewModel.updateUiState(
                        videoUiState.value.copy(
                            speakersNationality = null
                        )
                    )
                },
                onLinkValueChange = videoViewModel::loadVideoData
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
                videoViewModel.toggleDeleteDialogVisibility(false)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VideoEditBody(
    modifier: Modifier = Modifier,
    videoUiState: VideoUiState,
    onCourseValueChange: (VideoUiState) -> Unit,
    onDateChipClicked: () -> Unit,
    onDateClearClicked: () -> Unit,
    onLanguageValueChange: (Country) -> Unit,
    onLanguageClearClicked: () -> Unit,
    onLinkValueChange: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium)
    ) {
        item {
            OutlinedTextField(
                modifier = Modifier
                    .padding(
                        top = MaterialTheme.spacing.small,
                        bottom = MaterialTheme.spacing.small
                    )
                    .fillMaxWidth(),
                label = { Text(stringResource(R.string.video_link_label)) },
                value = videoUiState.videoUrl,
                onValueChange = { link: String ->
                    onCourseValueChange(
                        videoUiState.copy(
                            videoUrl = link
                        )
                    )
                    onLinkValueChange()
                },
                singleLine = true
            )
        }
        item {
            FlowRow(
                Modifier
                    .fillMaxWidth(1f)
                    .wrapContentHeight(align = Alignment.Top),
                horizontalArrangement = Arrangement.Start,
            ) {
                InputChip(
                    modifier = Modifier.padding(MaterialTheme.spacing.extraSmall),
                    onClick = onDateChipClicked,
                    label = {
                        if (videoUiState.watchedOn != null) {
                            Text(dateFormatter.format(videoUiState.watchedOn))
                        } else {
                            Text(stringResource(R.string.video_watched_on_label))
                        }
                    },
                    selected = videoUiState.watchedOn != null,
                    leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                    trailingIcon = {
                        if (videoUiState.watchedOn != null) {
                            Icon(
                                modifier = Modifier.clickable(onClick = onDateClearClicked),
                                imageVector = Icons.Filled.Clear,
                                contentDescription = null
                            )
                        }
                    }
                )
                CountryChoiceDropdownField(
                    onValueChange = onLanguageValueChange,
                    onClearClicked = onLanguageClearClicked,
                    videoUiState = videoUiState
                )
            }
        }
        item {
            VideoThumbnail(
                videoUrl = videoUiState.thumbnailHighUrl,
                duration = videoUiState.durationInSeconds.toLong()
            )
        }
        item {
            Text(
                modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                text = videoUiState.title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Left
            )
        }
        item {
            Text("${stringResource(R.string.video_channel_label)}: ${videoUiState.channel}")
        }
        item {
            Text("${stringResource(R.string.video_duration_label)}: ${videoUiState.durationInSeconds}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryChoiceDropdownField(
    modifier: Modifier = Modifier,
    videoUiState: VideoUiState,
    onValueChange: (Country) -> Unit,
    onClearClicked: () -> Unit,
    options: List<Country> = Country.values().asList(),
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
    ) {
        InputChip(
            modifier = Modifier
                .padding(MaterialTheme.spacing.extraSmall),
            onClick = { expanded = !expanded },
            label = {
                if (videoUiState.speakersNationality != null) {
                    Text(stringResource(videoUiState.speakersNationality.countryNameResId))
                } else {
                    Text(stringResource(R.string.video_country_label))
                }
            },
            selected = videoUiState.speakersNationality != null,
            leadingIcon = {
                if (videoUiState.speakersNationality != null) {
                    Text(videoUiState.speakersNationality.flagEmoji)
                } else {
                    Icon(Icons.Filled.Language, contentDescription = null)
                }
            },
            trailingIcon = {
                if (videoUiState.speakersNationality != null) {
                    Icon(
                        modifier = Modifier.clickable(onClick = onClearClicked),
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null
                    )
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text("${selectionOption.flagEmoji} ${stringResource(selectionOption.countryNameResId)}") },
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}