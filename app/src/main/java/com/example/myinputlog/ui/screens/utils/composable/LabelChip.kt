package com.example.myinputlog.ui.screens.utils.composable


import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ClickableLabelChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    title: String,
    backgroundColor: Color,
    textColor: Color
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(title) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = backgroundColor,
            labelColor = textColor,
            leadingIconContentColor = textColor,
            trailingIconContentColor = textColor
        ),
        border = null
    )
}