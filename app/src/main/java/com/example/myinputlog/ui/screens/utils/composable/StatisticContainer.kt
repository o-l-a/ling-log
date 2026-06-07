package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing

@Composable
fun StatisticContainer(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    leadingContent: @Composable () -> Unit,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.6f
            )
        )
    ) {
        Box(modifier = Modifier
            .clickable(enabled = isClickable) { onClick() }
            .fillMaxSize()) {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.default),
                leadingContent = leadingContent,
                headlineContent = {
                    Text(
                        text = number, style = MaterialTheme.typography.labelLarge
                    )
                },
                supportingContent = {
                    if (label.isNotBlank()) {
                        Text(
                            text = label, style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

@Preview
@Composable
fun StatisticContainerPreview() {
    MyInputLogTheme {
        Surface {
            StatisticContainer(
                modifier = Modifier
                    .width(MaterialTheme.spacing.doubleExtraLarge)
                    .height(MaterialTheme.spacing.extraLarge),
                number = "10",
                label = "label",
                leadingContent = {
                    Icon(imageVector = Icons.Filled.Timer, contentDescription = null)
                })
        }
    }
}