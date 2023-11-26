package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.ext.shimmerEffect
import com.example.myinputlog.ui.theme.spacing

@Composable
fun ListItemPlaceholder(
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                modifier = Modifier
                    .shimmerEffect(),
                text = stringResource(R.string.placeholder_long_text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Transparent)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        },
        supportingContent = {
            Text(
                modifier = Modifier.shimmerEffect(),
                text = stringResource(R.string.placeholder_long_text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Transparent)
            )
        },
        leadingContent = {
            VideoThumbnailPlaceholder(
                modifier = Modifier.width(MaterialTheme.spacing.doubleExtraLarge)
            )
        },
        trailingContent = {
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
        }
    )
}