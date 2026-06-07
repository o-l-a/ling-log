package com.example.myinputlog.ui.screens.utils.composable.label

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.FocusedBorderThickness
import androidx.compose.material3.OutlinedTextFieldDefaults.UnfocusedBorderThickness
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelPickerTextField(
    modifier: Modifier = Modifier,
    searchQuery: String,
    selectedItems: Set<LabelUiModel>,
    suggestions: Set<LabelUiModel>,
    onQueryChange: (String) -> Unit,
    onItemSelected: (LabelUiModel) -> Unit,
    onItemRemoved: (LabelUiModel) -> Unit,
    label: String = "",
    placeholder: String = "",
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        BasicTextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            value = searchQuery,
            onValueChange = {
                onQueryChange(it)
                expanded = true
            },
            interactionSource = interactionSource,
            textStyle = LocalTextStyle.current,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = searchQuery,
                    label = { Text(label) },
                    placeholder = {Text(placeholder)},
                    innerTextField = {
                        LabelChipRow(
                            labels = selectedItems, onLabelClicked = onItemRemoved
                        )
                        innerTextField()
                    },
                    interactionSource = interactionSource,
                    enabled = true,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = OutlinedTextFieldDefaults.colors(),
                            shape = OutlinedTextFieldDefaults.shape,
                            focusedBorderThickness = FocusedBorderThickness,
                            unfocusedBorderThickness = UnfocusedBorderThickness,
                        )
                    },
                    contentPadding = OutlinedTextFieldDefaults.contentPadding()
                )
            })
        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = expanded && suggestions.isNotEmpty(), onDismissRequest = {
                expanded = false
            }) {
                LabelChipRow(
                    modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.small),
                    labels = suggestions,
                    onLabelClicked = onItemSelected
                )
        }
    }
}