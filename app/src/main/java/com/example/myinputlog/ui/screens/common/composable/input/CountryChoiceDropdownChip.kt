package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.theme.spacing


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryChoiceDropdownChip(
    modifier: Modifier = Modifier,
    speakersNationality: CountryUiModel?,
    onCountryValueChange: (CountryUiModel?) -> Unit,
    options: List<CountryUiModel> = emptyList(),
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.wrapContentSize(Alignment.TopStart)
    ) {
        InputChip(
            modifier = Modifier.padding(start = MaterialTheme.spacing.extraSmall),
            onClick = { expanded = !expanded },
            label = {
                if (speakersNationality != null) {
                    Text(speakersNationality.flagEmoji)
                } else {
                    Text(stringResource(R.string.video_country_label))
                }
            },
            selected = speakersNationality != null,
            leadingIcon = {
                if (speakersNationality != null) {
                    Text(speakersNationality.displayName)
                } else {
                    Icon(Icons.Filled.Language, contentDescription = null)
                }
            },
            trailingIcon = {
                if (speakersNationality != null) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Filled.Clear,
                        modifier = Modifier.clickable {
                            onCountryValueChange(null)
                        },
                    )
                }
            })
        DropdownMenu(
            expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.displayName) }, onClick = {
                    onCountryValueChange(selectionOption)
                    expanded = false
                }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}