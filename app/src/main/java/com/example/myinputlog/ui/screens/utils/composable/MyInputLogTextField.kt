package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.hairtracker.ui.screens.utils.hairTrackerTextFieldColors

@Composable
fun MyInputLogTextField(
    modifier: Modifier = Modifier,
    label: String = "",
    value: String = "",
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    isEmphasized: Boolean = false,
    isCapitalized: Boolean = false,
    isEditable: Boolean = true
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth(),
        value = value,
        placeholder = {
            Text(
                label,
                style = if (isEmphasized) MaterialTheme.typography.titleLarge else LocalTextStyle.current
            )
        },
        readOnly = !isEditable,
        leadingIcon = leadingIcon,
        onValueChange = { onValueChange(it) },
        colors = hairTrackerTextFieldColors(),
        textStyle = if (isEmphasized) MaterialTheme.typography.titleLarge else LocalTextStyle.current,
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = if (isCapitalized) KeyboardCapitalization.Words else KeyboardCapitalization.Sentences
        )
    )
}