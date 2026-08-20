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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.common.IME_ACTION_DONE
import com.example.myinputlog.ui.screens.common.IME_ACTION_NEXT
import com.example.myinputlog.ui.screens.common.MAX_LABEL_LENGTH
import com.example.myinputlog.ui.screens.common.composable.bars.MyInputLogTopAppBar
import com.example.myinputlog.ui.screens.common.composable.input.CheckBoxWithLabel
import com.example.myinputlog.ui.screens.common.composable.input.ColorStopInspector
import com.example.myinputlog.ui.screens.common.composable.input.ConfirmDeleteDialog
import com.example.myinputlog.ui.screens.common.composable.input.HctPickerBottomSheet
import com.example.myinputlog.ui.screens.common.composable.label.ClickableLabelChip
import com.example.myinputlog.ui.screens.common.composable.state.EmptyCollectionBox
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.screens.label.LabelViewModel.LabelUiEvent
import com.example.myinputlog.ui.theme.ColorHelpers
import com.example.myinputlog.ui.theme.spacing

private enum class PickerTarget { BACKGROUND, TEXT }

@Composable
fun LabelScreen(
    modifier: Modifier = Modifier, labelViewModel: LabelViewModel, onNavigateUp: () -> Unit
) {
    val labelUiState by labelViewModel.labelUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var activePickerTarget by remember { mutableStateOf<PickerTarget?>(null) }

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
                    onActiveColorHexChange = labelViewModel::onActiveColorHexChange,
                    onSelectActiveColorIndex = labelViewModel::onSelectActiveColorIndex,
                    onAddBackgroundColor = labelViewModel::onAddBackgroundColor,
                    onRemoveBackgroundColor = labelViewModel::onRemoveBackgroundColor,
                    onActiveTextColorHexChange = labelViewModel::onActiveTextColorHexChange,
                    onSelectActiveTextColorIndex = labelViewModel::onSelectActiveTextColorIndex,
                    onAddTextColor = labelViewModel::onAddTextColor,
                    onRemoveTextColor = labelViewModel::onRemoveTextColor,
                    onAutoCalculateChange = labelViewModel::onAutoCalculateChange,
                    onOpenColorPicker = { target -> activePickerTarget = target },
                    onDone = labelViewModel::saveLabel
                )

                activePickerTarget?.let { target ->
                    val isBackground = target == PickerTarget.BACKGROUND
                    val activeIndex = if (isBackground) {
                        currentState.label.activeColorIndex
                    } else {
                        currentState.label.activeTextColorIndex
                    }
                    val initialHex = if (isBackground) {
                        currentState.label.colorsHex.getOrElse(activeIndex) { "FFFFC0CB" }
                    } else {
                        currentState.label.textColorsHex.getOrElse(activeIndex) { "FF000000" }
                    }
                    val sheetTitle = if (isBackground) {
                        "${stringResource(R.string.label_color_label)} #${activeIndex + 1}"
                    } else {
                        "${stringResource(R.string.label_text_color_label)} #${activeIndex + 1}"
                    }

                    HctPickerBottomSheet(
                        title = sheetTitle,
                        labelTitle = currentState.label.title,
                        initialColorHex = initialHex,
                        backgroundColorsHex = currentState.label.colorsHex,
                        textColorsHex = currentState.label.textColorsHex,
                        isEditingBackground = isBackground,
                        activeColorIndex = activeIndex,
                        onApply = { confirmedHex ->
                            if (isBackground) {
                                labelViewModel.onActiveColorHexChange(confirmedHex)
                            } else {
                                labelViewModel.onActiveTextColorHexChange(confirmedHex)
                            }
                            activePickerTarget = null
                        },
                        onDismiss = { activePickerTarget = null })
                }

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
private fun LabelEditBody(
    modifier: Modifier = Modifier,
    label: LabelForm,
    onTitleChange: (String) -> Unit,
    onActiveColorHexChange: (String) -> Unit,
    onSelectActiveColorIndex: (Int) -> Unit,
    onAddBackgroundColor: () -> Unit,
    onRemoveBackgroundColor: (Int) -> Unit,
    onActiveTextColorHexChange: (String) -> Unit,
    onSelectActiveTextColorIndex: (Int) -> Unit,
    onAddTextColor: () -> Unit,
    onRemoveTextColor: (Int) -> Unit,
    onAutoCalculateChange: (Boolean) -> Unit,
    onOpenColorPicker: (PickerTarget) -> Unit,
    onDone: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val previewData by remember(label.colorsHex, label.textColorsHex) {
        derivedStateOf {
            val bgColors =
                label.colorsHex.mapNotNull { ColorHelpers.hexToLong(it)?.let { c -> Color(c) } }
            val txtColors =
                label.textColorsHex.mapNotNull { ColorHelpers.hexToLong(it)?.let { c -> Color(c) } }

            if (bgColors.isNotEmpty() && txtColors.isNotEmpty()) {
                bgColors to txtColors
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
            onValueChange = { onTitleChange(it.take(MAX_LABEL_LENGTH)) },
            singleLine = true,
            keyboardOptions = IME_ACTION_NEXT,
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(top = MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                stringResource(R.string.preview_text),
                modifier = Modifier.padding(end = MaterialTheme.spacing.small)
            )
            AnimatedVisibility(
                visible = previewData != null,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
                label = "LabelPreviewAnimation"
            ) {
                previewData?.let { (bgColors, txtColors) ->
                    ClickableLabelChip(
                        onClick = { },
                        title = label.title,
                        backgroundColors = bgColors,
                        textColors = txtColors
                    )
                }
            }
        }
        ColorStopInspector(
            title = stringResource(R.string.label_color_label),
            colorsHex = label.colorsHex,
            activeColorIndex = label.activeColorIndex,
            enabled = true,
            onSelectIndex = onSelectActiveColorIndex,
            onHexChange = onActiveColorHexChange,
            onAddColor = onAddBackgroundColor,
            onRemoveColor = onRemoveBackgroundColor,
            onOpenColorPicker = { onOpenColorPicker(PickerTarget.BACKGROUND) })

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallPlus))

        CheckBoxWithLabel(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            value = label.autoCalculateTextColor,
            onValueChange = onAutoCalculateChange,
            text = stringResource(R.string.label_auto_text_color)
        )

        ColorStopInspector(
            title = stringResource(R.string.label_text_color_label),
            colorsHex = label.textColorsHex,
            activeColorIndex = label.activeTextColorIndex,
            enabled = !label.autoCalculateTextColor,
            onSelectIndex = onSelectActiveTextColorIndex,
            onHexChange = onActiveTextColorHexChange,
            onAddColor = onAddTextColor,
            onRemoveColor = onRemoveTextColor,
            onOpenColorPicker = { onOpenColorPicker(PickerTarget.TEXT) },
            keyboardOptions = IME_ACTION_DONE,
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone()
                    focusManager.clearFocus()
                })
        )
    }
}