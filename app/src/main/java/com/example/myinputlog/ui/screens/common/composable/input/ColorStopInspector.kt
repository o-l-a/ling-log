package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.screens.common.composable.ColorSwatch
import com.example.myinputlog.ui.theme.ColorHelpers
import com.example.myinputlog.ui.theme.spacing

private const val MAX_COLOR_STOPS = 20
private const val MIN_COLOR_STOPS = 1

@Composable
fun ColorStopInspector(
    modifier: Modifier = Modifier,
    title: String,
    colorsHex: List<String>,
    activeColorIndex: Int,
    enabled: Boolean = true,
    onSelectIndex: (Int) -> Unit,
    onHexChange: (String) -> Unit,
    onAddColor: () -> Unit,
    onRemoveColor: (Int) -> Unit,
    onOpenColorPicker: (Int) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val scrollState = rememberScrollState()

    val safeActiveIndex = activeColorIndex.coerceIn(0, (colorsHex.size - 1).coerceAtLeast(0))
    val currentHex = colorsHex.getOrElse(safeActiveIndex) { "" }
    val parsedColorLong = remember(currentHex) { ColorHelpers.hexToLong(currentHex) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.medium
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$title (${colorsHex.size}/$MAX_COLOR_STOPS)",
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(
                    visible = colorsHex.size > MIN_COLOR_STOPS, enter = fadeIn(), exit = fadeOut()
                ) {
                    IconButton(
                        onClick = { onRemoveColor(safeActiveIndex) },
                        enabled = enabled && colorsHex.size > MIN_COLOR_STOPS
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "delete color"
                        )
                    }
                }

                IconButton(
                    onClick = onAddColor, enabled = enabled && colorsHex.size < MAX_COLOR_STOPS
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "add color",
                        tint = if (enabled && colorsHex.size < MAX_COLOR_STOPS) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            colorsHex.forEachIndexed { index, hex ->
                val isSelected = index == safeActiveIndex
                val stopColorLong = remember(hex) { ColorHelpers.hexToLong(hex) }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(MaterialTheme.spacing.extraSmall + 2.dp))
                        .then(
                            if (isSelected && enabled) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall + 2.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .padding(2.dp)
                        .clickable(
                            enabled = enabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) {
                            if (isSelected) {
                                onOpenColorPicker(index)
                            } else {
                                onSelectIndex(index)
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    ColorSwatch(colorLong = stopColorLong)
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = currentHex,
            onValueChange = onHexChange,
            enabled = enabled,
            singleLine = true,
            label = { Text(text = "$title #${safeActiveIndex + 1}") },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .padding(end = MaterialTheme.spacing.small)
                        .clickable(
                            enabled = enabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false)
                        ) {
                            onOpenColorPicker(safeActiveIndex)
                        }) {
                    ColorSwatch(colorLong = parsedColorLong)
                }
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}