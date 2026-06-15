package com.example.myinputlog.ui.screens.common.composable.label

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelPickerTextField(
    modifier: Modifier = Modifier,
    searchQuery: String,
    suggestions: Set<LabelUiModel>,
    onQueryChange: (String) -> Unit,
    onItemSelected: (LabelUiModel) -> Unit,
    placeholder: String = "",
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier, expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            value = searchQuery,
            onValueChange = onQueryChange,
            trailingIcon = {},
            placeholder = { Text(placeholder) },
            textStyle = LocalTextStyle.current
        )
        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = {
                expanded = false
            }) {
            LabelChipRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.small),
                labels = suggestions,
                onLabelClicked = onItemSelected
            )
        }
    }
}