package com.example.myinputlog.ui.screens.common.composable.label

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myinputlog.ui.theme.ColorHelpers.getCompensatedGradientBrush
import com.example.myinputlog.ui.theme.spacing

@Composable
fun SmallLabelChip(
    modifier: Modifier = Modifier,
    title: String,
    backgroundColors: List<Color>,
    textColors: List<Color>
) {
    val isGradientBackground = backgroundColors.size > 1
    val isGradientText = textColors.size > 1

    val bgBrush = remember(backgroundColors) {
        if (isGradientBackground) getCompensatedGradientBrush(backgroundColors) else null
    }
    val txtBrush = remember(textColors) {
        if (isGradientText) getCompensatedGradientBrush(textColors) else null
    }

    val shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
    val solidBgColor = backgroundColors.firstOrNull() ?: Color.Transparent
    val solidTxtColor = textColors.firstOrNull() ?: MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier.then(
            if (bgBrush != null) {
                Modifier.background(
                    brush = bgBrush, shape = shape
                )
            } else {
                Modifier.background(
                    color = solidBgColor, shape = shape
                )
            }
        ), contentAlignment = Alignment.Center
    ) {
        if (isGradientText) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall),
                style = if (txtBrush != null) {
                    MaterialTheme.typography.labelSmall.copy(
                        brush = (txtBrush)
                    )
                } else {
                    MaterialTheme.typography.labelSmall
                }
            )
        } else {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall),
                style = MaterialTheme.typography.labelSmall,
                color = solidTxtColor
            )
        }
    }
}