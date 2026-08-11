package com.example.myinputlog.ui.screens.common.composable.country

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.theme.spacing

@Composable
fun CountryRepresentation(
    country: CountryUiModel, modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(country.flagEmoji)
        Spacer(Modifier.size(MaterialTheme.spacing.medium))
        Text(country.displayName)
    }
}