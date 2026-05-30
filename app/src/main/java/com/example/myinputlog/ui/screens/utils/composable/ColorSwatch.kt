package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.theme.spacing

@Composable
fun ColorSwatch(colorInt: Long) {
    Box(
        modifier = Modifier
            .size(MaterialTheme.spacing.medium)
            .clip(RoundedCornerShape(MaterialTheme.spacing.extraSmall))
            .background(Color(colorInt))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
            )
    )
}