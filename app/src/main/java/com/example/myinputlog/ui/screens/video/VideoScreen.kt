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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.screens.utils.Country
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.MyInputLogDropdownField
import com.example.myinputlog.ui.screens.utils.composable.VideoThumbnail
import com.example.myinputlog.ui.screens.utils.dateFormatter
import com.example.myinputlog.ui.screens.utils.getLanguageName
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch
import java.util.Date

object VideoDestination : NavigationDestination {
    override val route: String = "video"
    override val titleRes: Int = R.string.video_screen_title
    const val VIDEO_ID_ARG = "videoId"
    const val COURSE_ID_ARG = "courseId"
    const val VIDEO_URL_ARG = "videoUrl"
    val routeWithArgs = "$route/{$COURSE_ID_ARG}/{$VIDEO_ID_ARG}?$VIDEO_URL_ARG={$VIDEO_URL_ARG}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    videoViewModel: VideoViewModel,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val videoUiState by videoViewModel.videoUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val datePickerState = rememberDatePickerState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarErrorMessage = stringResource(R.string.network_error)
    val snackbarWrongUrlMessage = stringResource(R.string.wrong_url_message)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        val successState = videoUiState as? VideoUiState.Success
        MyInputLogTopAppBar(
            title = "",
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            hasDeleteAction = successState?.id?.isNotBlank() ?: false,
            hasSaveAction = true,
            isFormValid = successState?.isFormValid ?: false,
            onDelete = { videoViewModel.toggleDeleteDialogVisibility(true) },
            onSave = {
                videoViewModel.persistVideo()
                navigateBack()
            },
            scrollBehavior = scrollBehavior
        )
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        when (videoUiState) {
            is VideoUiState.Loading -> {
                LoadingBox()
            }

            is VideoUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            is VideoUiState.Success -> {
                VideoEditBody(
                    modifier = Modifier.padding(innerPadding),
                    videoUiState = videoUiState as VideoUiState.Success,
                    onCourseValueChange = videoViewModel::updateUserCourse,
                    onDateChipClicked = { videoViewModel.toggleDatePickerDialogVisibility(true) },
                    onDateClearClicked = { videoViewModel.updateWatchedOn(null) },
                    onCountryValueChange = videoViewModel::updateLanguage,
                    onUrlClearClicked = videoViewModel::deleteUrlAndUrlData,
                    onUrlValueChange = {
                        videoViewModel.updateVideoUrl(it)
                        videoViewModel.loadVideoMetadata { returnCode ->
                            when (returnCode) {
                                0 -> Unit
                                1 -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(snackbarWrongUrlMessage)
                                    }
                                }

                                else -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(snackbarErrorMessage)
                                    }
                                }
                            }
                        }
                    })
            }
        }
    }
    if (videoUiState is VideoUiState.Success) {
        if ((videoUiState as VideoUiState.Success).videoUiFlags.isDeleteDialogVisible) {
            ConfirmDeleteVideoDialog(
                videoName = (videoUiState as VideoUiState.Success).videoMetadata.title,
                onConfirm = {
                    videoViewModel.deleteVideo()
                    onNavigateUp()
                },
                onDismiss = {
                    videoViewModel.toggleDeleteDialogVisibility(false)
                })
        }
        if ((videoUiState as VideoUiState.Success).videoUiFlags.isDatePickerDialogVisible) {
            MyDatePickerDialog(
                onConfirm = {
                videoViewModel.updateWatchedOn(datePickerState.selectedDateMillis)
                videoViewModel.toggleDatePickerDialogVisibility(false)
            }, onDismiss = {
                videoViewModel.toggleDatePickerDialogVisibility(false)
            }, datePickerState = datePickerState
            )
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun VideoEditBody(
    modifier: Modifier = Modifier,
    videoUiState: VideoUiState.Success,
    onCourseValueChange: (UserCourse) -> Unit,
    onDateChipClicked: () -> Unit,
    onDateClearClicked: () -> Unit,
    onUrlClearClicked: () -> Unit,
    onCountryValueChange: (Country?) -> Unit,
    onUrlValueChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium)
    ) {
        item {
            MyInputLogDropdownField(value = videoUiState.userCourses.firstOrNull { userCourse ->
                userCourse.id == videoUiState.videoUserDraft.selectedCourseId
            } ?: UserCourse(),
                onValueChange = onCourseValueChange,
                options = videoUiState.userCourses,
                isInTopBar = false,
                isEditable = videoUiState.id.isBlank())
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .padding(
                        top = MaterialTheme.spacing.small, bottom = MaterialTheme.spacing.small
                    )
                    .fillMaxWidth(),
                label = { Text(stringResource(R.string.video_link_label)) },
                trailingIcon = {
                    if (videoUiState.videoUserDraft.videoUrl.isNotBlank()) {
                        Icon(
                            modifier = Modifier.clickable(onClick = onUrlClearClicked),
                            imageVector = Icons.Filled.Clear,
                            contentDescription = null
                        )
                    }
                },
                value = videoUiState.videoUserDraft.videoUrl,
                onValueChange = { url: String ->
                    keyboardController?.hide()
                    onUrlValueChange(url)
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
                        if (videoUiState.videoUserDraft.watchedOn != null && videoUiState.videoUserDraft.watchedOn != Date(
                                0
                            )
                        ) {
                            Text(dateFormatter.format(videoUiState.videoUserDraft.watchedOn))
                        } else {
                            Text(stringResource(R.string.video_watched_on_label))
                        }
                    },
                    selected = videoUiState.videoUserDraft.watchedOn != null,
                    leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                    trailingIcon = {
                        if (videoUiState.videoUserDraft.watchedOn != null) {
                            Icon(
                                modifier = Modifier.clickable(onClick = onDateClearClicked),
                                imageVector = Icons.Filled.Clear,
                                contentDescription = null
                            )
                        }
                    })
                CountryChoiceDropdownField(
                    onCountryValueChange = onCountryValueChange, videoUiState = videoUiState
                )
            }
        }
        if (videoUiState.videoUserDraft.videoUrl.isNotBlank() && videoUiState.videoLoadState is VideoLoadState.Success) {
            item {
                VideoThumbnail(
                    videoUrl = videoUiState.videoMetadata.thumbnailHighUrl,
                    duration = videoUiState.videoMetadata.durationInSeconds
                )
            }
            item {
                Text(
                    modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                    text = videoUiState.videoMetadata.title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Left
                )
            }
            item {
                Text(
                    text = "${videoUiState.videoMetadata.channel} • ${getLanguageName(videoUiState.videoMetadata.defaultAudioLanguage)}",
                    style = MaterialTheme.typography.bodyMedium
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
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDismiss: () -> Unit, onConfirm: () -> Unit, datePickerState: DatePickerState
) {
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = onConfirm) {
            Text(stringResource(R.string.ok_text))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.cancel_text))
        }
    }) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryChoiceDropdownField(
    modifier: Modifier = Modifier,
    videoUiState: VideoUiState.Success,
    onCountryValueChange: (Country?) -> Unit,
    options: List<Country> = Country.entries,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.wrapContentSize(Alignment.TopStart)
    ) {
        InputChip(
            modifier = Modifier.padding(MaterialTheme.spacing.extraSmall),
            onClick = { expanded = !expanded },
            label = {
                if (videoUiState.videoUserDraft.speakersNationality != null) {
                    Text(stringResource(videoUiState.videoUserDraft.speakersNationality.countryNameResId))
                } else {
                    Text(stringResource(R.string.video_country_label))
                }
            },
            selected = videoUiState.videoUserDraft.speakersNationality != null,
            leadingIcon = {
                if (videoUiState.videoUserDraft.speakersNationality != null) {
                    Text(videoUiState.videoUserDraft.speakersNationality.flagEmoji)
                } else {
                    Icon(Icons.Filled.Language, contentDescription = null)
                }
            },
            trailingIcon = {
                if (videoUiState.videoUserDraft.speakersNationality != null) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Filled.Clear,
                        modifier = Modifier.clickable {
                            onCountryValueChange(null)
                        },
                    )
                }
            })
        DropdownMenu(
            expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text("${selectionOption.flagEmoji} ${stringResource(selectionOption.countryNameResId)}") },
                    onClick = {
                        onCountryValueChange(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}