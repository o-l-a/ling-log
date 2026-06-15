package com.example.myinputlog.ui.screens.common.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.theme.spacing

@Composable
fun ColorSwatch(colorLong: Long?) {
    val backgroundColor = colorLong?.let { Color(it) } ?: Color(0x00FFFFFF)
    val strokeColor = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .size(MaterialTheme.spacing.medium)
            .clip(RoundedCornerShape(MaterialTheme.spacing.extraSmall))
            .background(backgroundColor)
            .then(
                if (colorLong == null) {
                Modifier.drawBehind {
                    val strokeWidth = 0.5.dp.toPx()
                    drawLine(
                        color = strokeColor,
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = strokeColor,
                        start = Offset(size.width, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            } else {
                Modifier
            })
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
            ))
}