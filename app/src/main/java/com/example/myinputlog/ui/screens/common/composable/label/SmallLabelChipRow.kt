package com.example.myinputlog.ui.screens.common.composable.label

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmallLabelChipRow(
    modifier: Modifier = Modifier, labels: List<LabelUiModel>, extraItemCount: Int = 0
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides MaterialTheme.spacing.default) {
        ContextualFlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            maxLines = 1,
            itemCount = labels.size,
            overflow = ContextualFlowRowOverflow.expandIndicator {
                val remaining = (labels.size + extraItemCount) - shownItemCount
                if (remaining > 0) {
                    SmallLabelChip(
                        modifier = Modifier.height(MaterialTheme.spacing.medium),
                        title = "+$remaining",
                        backgroundColors = listOf(MaterialTheme.colorScheme.surfaceVariant),
                        textColors = listOf(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }) { index ->
            val label = labels.getOrNull(index)
            label?.let {
                SmallLabelChip(
                    modifier = Modifier.height(MaterialTheme.spacing.medium),
                    title = label.title,
                    backgroundColors = remember(label.gradientColors) {
                        label.gradientColors.map { Color(it) }
                    },
                    textColors = remember(label.gradientTextColors) {
                        label.gradientTextColors.map { Color(it) }
                    })
            }
        }
    }
}