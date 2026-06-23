package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
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
import com.example.myinputlog.ui.models.CountryUiModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryChoiceDropdownField(
    modifier: Modifier = Modifier,
    selectedCountry: CountryUiModel?,
    onValueChange: (CountryUiModel) -> Unit,
    options: List<CountryUiModel> = emptyList(),
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            value = selectedCountry?.displayName ?: "",
            enabled = true,
            onValueChange = {},
            leadingIcon = {
                Text(selectedCountry?.flagEmoji ?: "")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            label = { Text(stringResource(R.string.channel_default_language)) },
            colors = OutlinedTextFieldDefaults.colors(),
            textStyle = LocalTextStyle.current
        )
        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    leadingIcon = {
                    Text(selectionOption.flagEmoji)
                }, text = { Text(selectionOption.displayName) }, onClick = {
                    onValueChange(selectionOption)
                    expanded = false
                }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}