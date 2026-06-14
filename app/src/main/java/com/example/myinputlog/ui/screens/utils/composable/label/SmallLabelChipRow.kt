package com.example.myinputlog.ui.screens.utils.composable.label

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmallLabelChipRow(
    modifier: Modifier = Modifier,
    labels: List<LabelUiModel>,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        ContextualFlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            maxLines = 1,
            itemCount = labels.size,
            overflow = ContextualFlowRowOverflow.expandIndicator {
                val remaining = labels.size - shownItemCount
                if (remaining > 0) {
                    SmallLabelChip(
                        modifier = Modifier.height(16.dp),
                        title = "+$remaining",
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }) { index ->
            val label = labels[index]
            SmallLabelChip(
                modifier = Modifier.height(MaterialTheme.spacing.medium),
                title = label.title,
                backgroundColor = Color(label.color),
                textColor = Color(label.textColor)
            )
        }
    }
}