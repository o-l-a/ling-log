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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    val context = LocalContext.current

    val isFormValid = remember(videoUiState) {
        (videoUiState as? VideoUiState.Success)?.isFormValid ?: false
    }

    LaunchedEffect(Unit) {
        videoViewModel.uiEvent.collect { event ->
            when (event) {
                is VideoViewModel.VideoUiEvent.ShowSnackbar -> {
                    val message = event.message.asString(context)
                    snackbarHostState.showSnackbar(message)
                }
                VideoViewModel.VideoUiEvent.NavigateBack -> {
                    navigateBack()
                }
            }
        }
    }

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        val successState = videoUiState as? VideoUiState.Success
        MyInputLogTopAppBar(
            title = "",
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            hasDeleteAction = successState?.isDeleteEnabled ?: false,
            hasSaveAction = true,
            isFormValid = isFormValid,
            onDelete = { videoViewModel.toggleDeleteDialogVisibility(true) },
            onSave = videoViewModel::persistVideo,
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
                    onUrlValueChange = videoViewModel::updateVideoUrl
                )
            }
        }
    }
    if (videoUiState is VideoUiState.Success) {
        VideoScreenDialogs(
            onDeleteConfirm = {
                videoViewModel.deleteVideo()
                onNavigateUp()
            },
            onDeleteDismiss = {
                videoViewModel.toggleDeleteDialogVisibility(false)
            },
            onDatePickerConfirm = {
                videoViewModel.updateWatchedOn(datePickerState.selectedDateMillis)
                videoViewModel.toggleDatePickerDialogVisibility(false)
            },
            onDatePickerDismiss = {
                videoViewModel.toggleDatePickerDialogVisibility(false)
            },
            datePickerState = datePickerState,
            videoUiState = videoUiState as VideoUiState.Success
        )
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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium)
    ) {
        item (key="course_input") {
            MyInputLogDropdownField(
                value = videoUiState.videoUserDraft.selectedCourse,
                onValueChange = onCourseValueChange,
                options = videoUiState.userCourses,
                isInTopBar = false,
                isEditable = videoUiState.isCourseEditable
            )
        }

        videoUrlSection(
            videoUrl = videoUiState.videoUserDraft.videoUrl,
            onUrlClearClicked = onUrlClearClicked,
            onUrlValueChange = onUrlValueChange
        )

        videoAttributesSection(
            watchedOn = videoUiState.videoUserDraft.watchedOn,
            speakersNationality = videoUiState.videoUserDraft.speakersNationality,
            onDateChipClicked = onDateChipClicked,
            onDateClearClicked = onDateClearClicked,
            onCountryValueChange = onCountryValueChange
        )

        videoMetadataSection(
            videoMetadata = videoUiState.videoMetadata,
            isVisible = videoUiState.isFormValid
        )
    }
}

fun LazyListScope.videoUrlSection(
    modifier: Modifier = Modifier,
    videoUrl: String,
    onUrlClearClicked: () -> Unit,
    onUrlValueChange: (String) -> Unit
) {
    item (key="url_input") {
        val keyboardController = LocalSoftwareKeyboardController.current
        OutlinedTextField(
            modifier = modifier
                .padding(
                    top = MaterialTheme.spacing.small, bottom = MaterialTheme.spacing.small
                )
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.video_link_label)) },
            trailingIcon = {
                if (videoUrl.isNotBlank()) {
                    Icon(
                        modifier = Modifier.clickable(onClick = onUrlClearClicked),
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            value = videoUrl,
            onValueChange = onUrlValueChange,
            singleLine = true
        )
    }
}

fun LazyListScope.videoAttributesSection(
    modifier: Modifier = Modifier,
    watchedOn: Date?,
    speakersNationality: Country?,
    onDateChipClicked: () -> Unit,
    onDateClearClicked: () -> Unit,
    onCountryValueChange: (Country?) -> Unit,
) {
    item (key="input_chips") {
        FlowRow(
            modifier
                .fillMaxWidth(1f)
                .wrapContentHeight(align = Alignment.Top),
            horizontalArrangement = Arrangement.Start,
        ) {
            InputChip(
                modifier = Modifier.padding(MaterialTheme.spacing.extraSmall),
                onClick = onDateChipClicked,
                label = {
                    if (watchedOn != null && watchedOn != Date(
                            0
                        )
                    ) {
                        Text(dateFormatter.format(watchedOn))
                    } else {
                        Text(stringResource(R.string.video_watched_on_label))
                    }
                },
                selected = watchedOn != null,
                leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                trailingIcon = {
                    if (watchedOn != null) {
                        Icon(
                            modifier = Modifier.clickable(onClick = onDateClearClicked),
                            imageVector = Icons.Filled.Clear,
                            contentDescription = null
                        )
                    }
                })
            CountryChoiceDropdownField(
                onCountryValueChange = onCountryValueChange,
                speakersNationality = speakersNationality
            )
        }
    }
}

fun LazyListScope.videoMetadataSection(
    videoMetadata: VideoMetadata, isVisible: Boolean
) {
    if (isVisible) {
        item (key="video_thumbnail") {
            VideoThumbnail(
                videoUrl = videoMetadata.thumbnailMediumUrl,
                duration = videoMetadata.durationInSeconds
            )
        }
        item (key="video_title") {
            Text(
                modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                text = videoMetadata.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Left
            )
        }
        item (key="channel_info") {
            Text(
                text = "${videoMetadata.channel} • ${getLanguageName(videoMetadata.defaultAudioLanguage)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun VideoScreenDialogs(
    modifier: Modifier = Modifier,
    videoUiState: VideoUiState.Success,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDatePickerDismiss: () -> Unit,
    onDatePickerConfirm: () -> Unit,
    datePickerState: DatePickerState
) {
    if (videoUiState.videoUiFlags.isDeleteDialogVisible) {
        ConfirmDeleteVideoDialog(
            modifier = modifier,
            videoName = videoUiState.videoMetadata.title,
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss
        )
    }
    if (videoUiState.videoUiFlags.isDatePickerDialogVisible) {
        MyDatePickerDialog(
            onDismiss = onDatePickerDismiss,
            onConfirm = onDatePickerConfirm,
            datePickerState = datePickerState
        )
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
    speakersNationality: Country?,
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
                if (speakersNationality != null) {
                    Text(stringResource(speakersNationality.countryNameResId))
                } else {
                    Text(stringResource(R.string.video_country_label))
                }
            },
            selected = speakersNationality != null,
            leadingIcon = {
                if (speakersNationality != null) {
                    Text(speakersNationality.flagEmoji)
                } else {
                    Icon(Icons.Filled.Language, contentDescription = null)
                }
            },
            trailingIcon = {
                if (speakersNationality != null) {
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