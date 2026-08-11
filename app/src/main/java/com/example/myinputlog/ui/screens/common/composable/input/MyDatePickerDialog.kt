package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R

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