package com.example.myinputlog.ui.screens.label

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.label.LabelViewModel.LabelUiEvent
import com.example.myinputlog.ui.screens.utils.IME_ACTION_DONE
import com.example.myinputlog.ui.screens.utils.IME_ACTION_NEXT
import com.example.myinputlog.ui.screens.utils.composable.ClickableLabelChip
import com.example.myinputlog.ui.screens.utils.composable.ColorSwatch
import com.example.myinputlog.ui.screens.utils.composable.ConfirmDeleteDialog
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.theme.spacing

@Composable
fun LabelScreen(
    modifier: Modifier = Modifier, labelViewModel: LabelViewModel, onNavigateUp: () -> Unit
) {
    val labelUiState by labelViewModel.labelUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        labelViewModel.uiEvent.collect { event ->
            when (event) {
                is LabelUiEvent.NavigateBack -> {
                    onNavigateUp()
                }
            }
        }
    }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            val successState = labelUiState as? LabelUiState.Success
            MyInputLogTopAppBar(
                title = stringResource(R.string.label_nav_description),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                hasDeleteAction = successState?.label?.id?.isNotBlank() ?: false,
                hasSaveAction = true,
                isFormValid = successState?.isFormValid ?: false,
                onDelete = { labelViewModel.toggleDialogVisibility(true) },
                onSave = labelViewModel::saveLabel,
                scrollBehavior = scrollBehavior
            )
        }) { innerPadding ->
        when (val currentState = labelUiState) {
            is LabelUiState.Loading -> {
                LoadingBox()
            }

            is LabelUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            is LabelUiState.Success -> {
                LabelEditBody(
                    modifier = Modifier.padding(innerPadding),
                    label = currentState.label,
                    onTitleChange = labelViewModel::onTitleChange,
                    onColorChange = labelViewModel::onColorChange,
                    onTextColorChange = labelViewModel::onTextColorChange,
                    onAutoCalculateChange = labelViewModel::onAutoCalculateChange,
                    onDone = labelViewModel::saveLabel
                )

                if (currentState.isDialogVisible) {
                    ConfirmDeleteDialog(entityName = currentState.label.title, text = {
                        Text(
                            stringResource(
                                R.string.delete_label_phrase, currentState.label.title
                            )
                        )
                    }, onConfirm = labelViewModel::deleteLabel, onDismiss = {
                        labelViewModel.toggleDialogVisibility(false)
                    })
                }
            }
        }
    }
}

@Composable
fun LabelEditBody(
    modifier: Modifier = Modifier,
    label: LabelForm,
    onTitleChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onTextColorChange: (String) -> Unit,
    onAutoCalculateChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val previewData by remember(label.previewColor, label.previewTextColor) {
        derivedStateOf {
            val bgColor = label.previewColor
            val txtColor = label.previewTextColor
            if (bgColor != null && txtColor != null) {
                Color(bgColor) to Color(txtColor)
            } else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
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
            label = { Text(stringResource(R.string.label_name_label)) },
            value = label.title,
            onValueChange = onTitleChange,
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
            label = { Text(stringResource(R.string.label_color_label)) },
            value = label.colorHex,
            onValueChange = onColorChange,
            singleLine = true,
            trailingIcon = {
                ColorSwatch(label.previewColor)
            },
            keyboardOptions = IME_ACTION_NEXT.copy(
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MaterialTheme.spacing.large + MaterialTheme.spacing.medium)
                .toggleable(
                    value = label.autoCalculateTextColor,
                    role = Role.Checkbox,
                    onValueChange = { isChecked ->
                        onAutoCalculateChange(isChecked)
                    })
                .padding(horizontal = MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = label.autoCalculateTextColor, onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = stringResource(R.string.label_auto_text_color)
            )
        }
        OutlinedTextField(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.small
                )
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.label_text_color_label)) },
            value = label.textColorHex,
            enabled = !label.autoCalculateTextColor,
            onValueChange = onTextColorChange,
            singleLine = true,
            trailingIcon = {
                ColorSwatch(label.previewTextColor)
            },
            keyboardOptions = IME_ACTION_DONE.copy(
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone()
                    focusManager.clearFocus()
                })
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        val isVisible = previewData != null
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
            label = "LabelPreviewAnimation"
        ) {
            previewData?.let { (backgroundColor, textColor) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        stringResource(R.string.preview_text),
                        modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                    )
                    ClickableLabelChip(
                        onClick = { },
                        title = label.title,
                        backgroundColor = backgroundColor,
                        textColor = textColor
                    )
                }
            }
        }
    }
}