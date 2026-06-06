package com.example.myinputlog.ui.screens.label

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
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
                title = "",
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
                    labelUiState = currentState,
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
    labelUiState: LabelUiState.Success,
    onTitleChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onTextColorChange: (String) -> Unit,
    onAutoCalculateChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    val focusManager = LocalFocusManager.current

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
            value = labelUiState.label.title,
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
            value = labelUiState.label.colorHex,
            onValueChange = onColorChange,
            singleLine = true,
            trailingIcon = {
                ColorSwatch(labelUiState.label.previewColor)
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
                    value = labelUiState.label.autoCalculateTextColor,
                    role = Role.Checkbox,
                    onValueChange = { isChecked ->
                        onAutoCalculateChange(isChecked)
                    })
                .padding(horizontal = MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = labelUiState.label.autoCalculateTextColor, onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = stringResource(R.string.label_auto_text_color),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
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
            value = labelUiState.label.textColorHex,
            enabled = !labelUiState.label.autoCalculateTextColor,
            onValueChange = onTextColorChange,
            singleLine = true,
            trailingIcon = {
                ColorSwatch(labelUiState.label.previewTextColor)
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
    }
}