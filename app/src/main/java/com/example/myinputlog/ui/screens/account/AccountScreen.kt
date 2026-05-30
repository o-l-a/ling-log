package com.example.myinputlog.ui.screens.account

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LeadingIconWithText
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.screens.utils.composable.channel.ProfilePicture
import com.example.myinputlog.ui.screens.utils.ext.hideEmail
import com.example.myinputlog.ui.theme.spacing
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel,
    onNavigateUp: () -> Unit,
    navigateWithPopUp: () -> Unit
) {
    val accountUiState by accountViewModel.accountUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        accountViewModel.uiEvent.collect { event ->
            when (event) {
                is AccountViewModel.AccountUiEvent.ShowSnackbar -> {
                    val message = event.message.asString(context)
                    snackbarHostState.showSnackbar(message)
                }

                AccountViewModel.AccountUiEvent.NavigateWithPopUp -> {
                    navigateWithPopUp()
                }

                AccountViewModel.AccountUiEvent.NavigateUp -> {
                    onNavigateUp()
                }
            }
        }
    }

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        MyInputLogTopAppBar(
            title = "",
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            hasDeleteAction = false,
            hasSaveAction = true,
            isFormValid = (accountUiState as? AccountUiState.Success)?.isFormValid ?: false,
            onSave = accountViewModel::saveUsername,
            scrollBehavior = scrollBehavior
        )
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        when (val currentState = accountUiState) {
            is AccountUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            AccountUiState.Loading -> {
                LoadingBox()
            }

            is AccountUiState.Success -> {
                AccountBody(
                    Modifier.padding(innerPadding),
                    currentState,
                    onUsernameChange = accountViewModel::updateUsername,
                    onSignOutClicked = accountViewModel::signOut,
                    onPhotoSave = accountViewModel::saveProfilePhoto,
                    toggleDialogVisibility = accountViewModel::toggleDialogVisibility,
                )
            }
        }
        if (accountUiState is AccountUiState.Success && (accountUiState as AccountUiState.Success).isDialogVisible) {
            ConfirmDeleteAccountDialog(
                onConfirm = accountViewModel::deleteAccount,
                onDismiss = { accountViewModel.toggleDialogVisibility(false) })
        }
    }
}


@Composable
fun AccountBody(
    modifier: Modifier = Modifier,
    accountUiState: AccountUiState.Success,
    onSignOutClicked: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onPhotoSave: (Uri) -> Unit,
    toggleDialogVisibility: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPhotoSave(uri)
        }
    }

    Column(modifier = modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }
        .padding(MaterialTheme.spacing.extraSmall),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EditImageContainer(
                    Modifier.size(
                        MaterialTheme.spacing.doubleExtraLarge + MaterialTheme.spacing.large
                    ), accountUiState.username, accountUiState.imagePath
                ) { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                Text(
                    if (accountUiState.hideEmail) accountUiState.email.hideEmail() else accountUiState.email,
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
                    label = { Text(stringResource(R.string.username)) },
                    value = accountUiState.username,
                    onValueChange = onUsernameChange,
                    singleLine = true
                )
            }
        }
        Row {
            Column {
                FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    onClick = { onSignOutClicked() }) {
                    Text(
                        text = stringResource(R.string.log_out)
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    onClick = { toggleDialogVisibility(true) }) {
                    Text(
                        text = stringResource(R.string.delete_account)
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }
}

@Composable
private fun ConfirmDeleteAccountDialog(
    modifier: Modifier = Modifier, onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_account_dialog_title)) },
        text = { Text(stringResource(R.string.delete_account_phrase)) },
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
                Text(text = stringResource(R.string.confirm_delete_account))
            }
        })
}

@Composable
private fun EditImageContainer(
    modifier: Modifier, username: String, imageFile: File?, onClick: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        if (imageFile != null) {
            ProfilePicture(
                Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.medium), imageFile
            )
        } else {
            LeadingIconWithText(
                Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.medium), name = username
            )
        }
        IconButton(
            onClick, Modifier
                .zIndex(1F)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit, contentDescription = "edit"
            )
        }
    }
}