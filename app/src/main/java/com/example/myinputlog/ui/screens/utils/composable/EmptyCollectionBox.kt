package com.example.myinputlog.ui.screens.utils.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.myinputlog.R
import com.example.myinputlog.ui.theme.spacing

@Composable
fun EmptyCollectionBox(
    modifier: Modifier = Modifier,
    isFullSize: Boolean = true,
    @StringRes bodyMessage: Int = R.string.empty_course_collection_body
) {
    Column(
        modifier = if (isFullSize) modifier.fillMaxSize() else modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.small),
            text = stringResource(R.string.empty_collection_headline),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(bodyMessage),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}