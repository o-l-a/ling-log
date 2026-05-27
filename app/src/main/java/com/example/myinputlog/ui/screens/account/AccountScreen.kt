package com.example.myinputlog.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.profile.ProfileUiState
import com.example.myinputlog.ui.screens.utils.IME_ACTION_DONE
import com.example.myinputlog.ui.theme.spacing

@Composable
fun AccountBody(
    modifier: Modifier = Modifier,
    onSignOutClicked: () -> Unit,
    toggleDialogVisibility: (Boolean) -> Unit
) {

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            HorizontalDivider()
        }
        item {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                onClick = { onSignOutClicked() }) {
                Text(
                    text = stringResource(R.string.log_out)
                )
            }
        }
        item {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                onClick = { toggleDialogVisibility(true) }) {
                Text(
                    text = stringResource(R.string.delete_account)
                )
            }
        }
    }
}

@Composable
fun EditUsernameDialog(
    modifier: Modifier = Modifier,
    profileUiState: ProfileUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onValueChange: (ProfileUiState) -> Unit
) {
    AlertDialog(modifier = modifier, onDismissRequest = onDismiss, text = {
        OutlinedTextField(
            value = profileUiState.newUsername,
            onValueChange = { onValueChange(profileUiState.copy(newUsername = it)) },
            label = {
                Text(stringResource(R.string.new_username))
            },
            keyboardOptions = IME_ACTION_DONE,
            keyboardActions = KeyboardActions(
                onDone = { onConfirm() }),
            textStyle = MaterialTheme.typography.bodyLarge)
    }, confirmButton = {
        TextButton(onClick = onConfirm) {
            Text(stringResource(R.string.save_text))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.dismiss_delete))
        }
    })
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