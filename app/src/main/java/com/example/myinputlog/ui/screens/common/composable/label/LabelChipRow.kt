package com.example.myinputlog.ui.screens.common.composable.label

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.theme.spacing

@Composable
fun LabelChipRow(
    modifier: Modifier = Modifier,
    labels: Set<LabelUiModel>,
    onLabelClicked: (LabelUiModel) -> Unit,
    isDeletable: Boolean = false
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides MaterialTheme.spacing.default) {
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            labels.forEach { label ->
                ClickableLabelChip(
                    modifier = Modifier.animateContentSize(),
                    onClick = { if (!isDeletable) onLabelClicked(label) },
                    title = label.title,
                    backgroundColors = remember(label.gradientColors) {
                        label.gradientColors.map { Color(it) }
                    },
                    textColors = remember(label.gradientTextColors) {
                        label.gradientTextColors.map { Color(it) }
                    },
                    trailingContent = {
                        AnimatedVisibility(
                            visible = isDeletable,
                            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                        ) {
                            Icon(
                                contentDescription = "delete",
                                imageVector = Icons.Filled.Clear,
                                modifier = Modifier.clickable {
                                    onLabelClicked(label)
                                },
                            )
                        }
                    })
            }
        }
    }
}