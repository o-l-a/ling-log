package com.example.myinputlog.ui.screens.utils.composable.label

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myinputlog.ui.theme.spacing

@Composable
fun SmallLabelChip(
    modifier: Modifier = Modifier, title: String, backgroundColor: Color, textColor: Color
) {
    Box(
        modifier = modifier.background(
            color = backgroundColor, shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
        ), contentAlignment = Alignment.Center
    ) {
        Text(
            text = title, modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.extraSmall
            ), style = MaterialTheme.typography.labelSmall, color = textColor
        )
    }
}