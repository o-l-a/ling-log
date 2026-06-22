package com.example.myinputlog.ui.screens.video

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.R
import com.example.myinputlog.data.utils.LanguageUtils.getLanguageDisplayName
import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.screens.common.MAX_URL_LENGTH
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogTopAppBar
import com.example.myinputlog.ui.screens.common.composable.input.CheckBoxWithLabel
import com.example.myinputlog.ui.screens.common.composable.input.ConfirmDeleteDialog
import com.example.myinputlog.ui.screens.common.composable.input.CountryChoiceDropdownField
import com.example.myinputlog.ui.screens.common.composable.input.MyDatePickerDialog
import com.example.myinputlog.ui.screens.common.composable.input.MyInputLogDropdownField
import com.example.myinputlog.ui.screens.common.composable.label.LabelChipRow
import com.example.myinputlog.ui.screens.common.composable.label.LabelPickerTextField
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.screens.common.composable.video.VideoThumbnail
import com.example.myinputlog.ui.screens.common.dateFormatter
import com.example.myinputlog.ui.theme.spacing
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    videoViewModel: VideoViewModel,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val videoUiState by videoViewModel.videoUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val datePickerState = rememberDatePickerState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboard.current
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
            title = stringResource(R.string.video_screen_title),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            hasDeleteAction = successState?.isDeleteEnabled ?: false,
            hasSaveAction = true,
            isFormValid = isFormValid && successState?.isSaveEnabled ?: false,
            onDelete = { videoViewModel.toggleDeleteDialogVisibility(true) },
            onSave = videoViewModel::saveVideo,
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
                    onUrlValueChange = videoViewModel::updateVideoUrl,
                    onCopyClicked = { url ->
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(url, url)
                                )
                            )
                        }
                    },
                    onQueryChange = videoViewModel::onQueryChange,
                    onItemRemoved = videoViewModel::removeLabel,
                    onItemSelected = videoViewModel::addLabel,
                    onEditStart = videoViewModel::startEdit,
                    onCreateChannelToggle = videoViewModel::onSyncLabelsChange
                )
            }
        }
    }
    if (videoUiState is VideoUiState.Success) {
        VideoScreenDialogs(
            onDeleteConfirm = {
            videoViewModel.deleteVideo()
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

@Composable
fun VideoEditBody(
    modifier: Modifier = Modifier,
    videoUiState: VideoUiState.Success,
    onCourseValueChange: (CourseUiModel) -> Unit,
    onDateChipClicked: () -> Unit,
    onDateClearClicked: () -> Unit,
    onUrlClearClicked: () -> Unit,
    onCopyClicked: (String) -> Unit,
    onCountryValueChange: (CountryUiModel?) -> Unit,
    onUrlValueChange: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onItemSelected: (LabelUiModel) -> Unit,
    onItemRemoved: (LabelUiModel) -> Unit,
    onEditStart: () -> Unit,
    onCreateChannelToggle: (Boolean) -> Unit
) {
    val scrollState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        state = scrollState,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium)
    ) {
        if (videoUiState.isCourseEditable) {
            item(key = "course_input") {
                MyInputLogDropdownField(
                    value = videoUiState.videoForm.selectedCourse,
                    onValueChange = onCourseValueChange,
                    options = videoUiState.userCourses,
                    isInTopBar = false,
                    isEditable = videoUiState.isCourseEditable
                )
            }
        }

        videoUrlSection(
            videoUrl = videoUiState.videoForm.videoUrl,
            isEditable = videoUiState.isCourseEditable,
            onUrlClearClicked = onUrlClearClicked,
            onUrlValueChange = onUrlValueChange,
            onCopyClicked = onCopyClicked,
        )

        videoAttributesSection(
            watchedOn = videoUiState.videoForm.watchedOn,
            speakersNationality = videoUiState.videoForm.speakersNationality,
            onDateChipClicked = onDateChipClicked,
            onDateClearClicked = onDateClearClicked,
            onCountryValueChange = onCountryValueChange
        )

        videoMetadataSection(
            videoMetadata = videoUiState.videoForm, isVisible = videoUiState.isFormValid
        )

        labelSection(
            videoMetadata = videoUiState.videoForm,
            isEditStarted = videoUiState.videoUiFlags.isEditStarted,
            isNewChannel = videoUiState.videoUiFlags.isNewChannel,
            createChannelWithLabels = videoUiState.videoForm.saveLabelsForChannel,
            suggestions = videoUiState.suggestions,
            onQueryChange = onQueryChange,
            onItemRemoved = onItemRemoved,
            onItemSelected = onItemSelected,
            onEditStart = onEditStart,
            onCreateChannelToggle = onCreateChannelToggle
        )
    }
}

fun LazyListScope.videoUrlSection(
    modifier: Modifier = Modifier,
    videoUrl: String,
    isEditable: Boolean,
    onUrlClearClicked: () -> Unit,
    onUrlValueChange: (String) -> Unit,
    onCopyClicked: (String) -> Unit,
) {
    item(key = "url_input") {
        val keyboardController = LocalSoftwareKeyboardController.current
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()

        val iconColor = OutlinedTextFieldDefaults.colors().trailingIconColor(
            enabled = true, isError = false, focused = isFocused
        )

        OutlinedTextField(
            modifier = modifier
                .padding(
                    top = MaterialTheme.spacing.small
                )
                .fillMaxWidth(),
            enabled = isEditable,
            label = { Text(stringResource(R.string.video_link_label)) },
            trailingIcon = {
                IconButton(onClick = {
                    if (!isEditable) {
                        onCopyClicked(videoUrl)
                    } else if (videoUrl.isNotBlank()) {
                        onUrlClearClicked()
                    }
                }) {
                    if (!isEditable) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            tint = iconColor
                        )
                    } else if (videoUrl.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Filled.Clear, contentDescription = null
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            value = videoUrl,
            onValueChange = { onUrlValueChange(it.take(MAX_URL_LENGTH)) },
            singleLine = true
        )
    }
}

fun LazyListScope.videoAttributesSection(
    modifier: Modifier = Modifier,
    watchedOn: Date?,
    speakersNationality: CountryUiModel?,
    onDateChipClicked: () -> Unit,
    onDateClearClicked: () -> Unit,
    onCountryValueChange: (CountryUiModel?) -> Unit,
) {
    item(key = "input_chips") {
        FlowRow(
            modifier
                .fillMaxWidth(1f)
                .padding(vertical = MaterialTheme.spacing.small)
                .wrapContentHeight(align = Alignment.Top),
            horizontalArrangement = Arrangement.Start,
        ) {
            InputChip(
                modifier = Modifier.padding(end = MaterialTheme.spacing.extraSmall),
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
    videoMetadata: VideoForm, isVisible: Boolean
) {
    val springSpec = spring<IntSize>(
        dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
    )
    if (isVisible) {
        item(key = "video_metadata") {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = spring()) + expandVertically(animationSpec = springSpec),
                exit = fadeOut(animationSpec = spring()) + shrinkVertically(animationSpec = springSpec),
                modifier = Modifier.animateItem()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                ) {
                    VideoThumbnail(
                        videoThumbnailUrl = videoMetadata.thumbnailHighUrl,
                        duration = videoMetadata.durationInSeconds
                    )
                    Text(
                        modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                        text = videoMetadata.title,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Left
                    )
                    Text(
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.small),
                        text = "${videoMetadata.channelCustomUrl} • ${videoMetadata.channelTitle} • ${
                            getLanguageDisplayName(
                                videoMetadata.defaultAudioLanguage
                            ) ?: stringResource(R.string.unknown_language)
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

fun LazyListScope.labelSection(
    videoMetadata: VideoForm,
    suggestions: Set<LabelUiModel>,
    isEditStarted: Boolean,
    isNewChannel: Boolean,
    createChannelWithLabels: Boolean,
    onQueryChange: (String) -> Unit,
    onItemSelected: (LabelUiModel) -> Unit,
    onItemRemoved: (LabelUiModel) -> Unit,
    onEditStart: () -> Unit,
    onCreateChannelToggle: (Boolean) -> Unit
) {
    item(key = "labels_row") {
        LabelChipRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = MaterialTheme.spacing.extraSmall, bottom = MaterialTheme.spacing.small
                )
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
                    )
                )
                .animateItem(),
            isDeletable = isEditStarted,
            labels = videoMetadata.selectedLabels,
            onLabelClicked = {
                if (isEditStarted) {
                    onItemRemoved(it)
                } else {
                    onEditStart()
                }
            })
    }
    item(key = "picker") {
        LabelPickerTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.extraSmall)
                .onFocusEvent { state ->
                    if (state.isFocused) {
                        onEditStart()
                    }
                }
                .animateItem(),
            placeholder = stringResource(R.string.labels_search_placeholder),
            searchQuery = videoMetadata.searchQuery,
            suggestions = suggestions,
            onQueryChange = { onQueryChange(it) },
            onItemSelected = { onItemSelected(it) },
        )
    }
    item(key = "checkbox") {
        AnimatedVisibility(isEditStarted && isNewChannel && videoMetadata.selectedLabels.isNotEmpty()) {
            CheckBoxWithLabel(
                value = createChannelWithLabels,
                onValueChange = onCreateChannelToggle,
                text = stringResource(R.string.video_create_channel_with_labels)
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
        ConfirmDeleteDialog(
            modifier = modifier, entityName = videoUiState.videoForm.title, text = {
                Text(
                    stringResource(
                        R.string.delete_video_phrase, videoUiState.videoForm.title
                    )
                )
            }, onConfirm = onDeleteConfirm, onDismiss = onDeleteDismiss
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