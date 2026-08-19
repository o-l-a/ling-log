package com.example.myinputlog.ui.screens.common.composable.label


import androidx.compose.foundation.background
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myinputlog.ui.theme.ColorHelpers.getCompensatedGradientBrush
import com.example.myinputlog.ui.theme.spacing

@Composable
fun ClickableLabelChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    title: String,
    backgroundColors: List<Color>,
    textColors: List<Color>,
    trailingContent: @Composable () -> Unit = {}
) {
    val isGradientBackground = backgroundColors.size > 1
    val isGradientText = textColors.size > 1

    val bgBrush = remember(backgroundColors) {
        if (isGradientBackground) getCompensatedGradientBrush(backgroundColors) else null
    }
    val txtBrush = remember(textColors) {
        if (isGradientText) getCompensatedGradientBrush(textColors) else null
    }

    val solidBgColor = backgroundColors.firstOrNull() ?: Color.Transparent
    val solidTxtColor = textColors.firstOrNull() ?: MaterialTheme.colorScheme.onSurface
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides MaterialTheme.spacing.default) {
        AssistChip(
            modifier = modifier.then(
                if (bgBrush != null) {
                    Modifier.background(
                        brush = bgBrush, shape = AssistChipDefaults.shape
                    )
                } else {
                    Modifier
                }
            ), onClick = onClick, label = {
                if (isGradientText) {
                    Text(
                        text = title, style = if (txtBrush != null) {
                            LocalTextStyle.current.copy(brush = txtBrush)
                        } else {
                            LocalTextStyle.current
                        }
                    )
                } else {
                    Text(
                        text = title, color = solidTxtColor
                    )
                }
            }, colors = AssistChipDefaults.assistChipColors(
                containerColor = if (isGradientBackground) Color.Transparent else solidBgColor,
                labelColor = solidTxtColor,
                leadingIconContentColor = solidTxtColor,
                trailingIconContentColor = solidTxtColor
            ), trailingIcon = trailingContent, border = null
        )
    }
}