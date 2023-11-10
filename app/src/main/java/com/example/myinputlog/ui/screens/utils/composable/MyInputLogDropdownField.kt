package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.screens.utils.myInputLogTextFieldColors

/**
 * A composable that holds the current selected course.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInputLogDropdownField(
    modifier: Modifier = Modifier,
    value: UserCourse?,
    onValueChange: (UserCourse) -> Unit,
    options: List<UserCourse>,
    isInTopBar: Boolean = true,
    isEditable: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = value?.name ?: "",
            enabled = isEditable,
            onValueChange = {},
            trailingIcon = {
                if (isEditable)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            label = { if (!isInTopBar) Text(stringResource(R.string.video_course_label)) },
            colors = if (isInTopBar) myInputLogTextFieldColors() else OutlinedTextFieldDefaults.colors(),
            textStyle = if (isInTopBar) MaterialTheme.typography.titleLarge else LocalTextStyle.current
        )
        if (isEditable) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.name) },
                        onClick = {
                            onValueChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}