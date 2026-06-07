package com.example.myinputlog.ui.screens.utils.composable.label

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.theme.spacing

@Composable
fun LabelChipRow(
    modifier: Modifier = Modifier,
    labels: Set<LabelUiModel>,
    onLabelClicked: (LabelUiModel) -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            labels.forEach { label ->
                ClickableLabelChip(
                    onClick = { onLabelClicked(label) },
                    title = label.title,
                    backgroundColor = Color(label.color),
                    textColor = Color(label.textColor)
                )
            }
        }
    }
}