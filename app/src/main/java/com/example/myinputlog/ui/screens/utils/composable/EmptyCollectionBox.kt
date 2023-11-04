package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R

@Composable
fun EmptyCollectionBox(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.empty_collection_headline),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(R.string.empty_collection_body),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}