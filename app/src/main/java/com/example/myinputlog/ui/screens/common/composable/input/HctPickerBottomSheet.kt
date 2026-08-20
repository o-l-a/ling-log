package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.common.composable.label.ClickableLabelChip
import com.example.myinputlog.ui.theme.ColorHelpers
import com.example.myinputlog.ui.theme.spacing
import com.materialkolor.hct.Hct
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HctPickerBottomSheet(
    title: String,
    labelTitle: String,
    initialColorHex: String,
    backgroundColorsHex: List<String>,
    textColorsHex: List<String>,
    isEditingBackground: Boolean,
    activeColorIndex: Int,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        confirmValueChange = { true })

    val initialColorInt = remember(initialColorHex) {
        ColorHelpers.hexToLong(initialColorHex)?.toInt() ?: 0xFF000000.toInt()
    }
    val initialHct = remember(initialColorInt) {
        Hct.fromInt(initialColorInt)
    }

    var hue by remember { mutableFloatStateOf(initialHct.hue.toFloat()) }
    var chroma by remember { mutableFloatStateOf(initialHct.chroma.toFloat()) }
    var tone by remember { mutableFloatStateOf(initialHct.tone.toFloat()) }

    val currentHct by remember {
        derivedStateOf {
            Hct.from(hue.toDouble(), chroma.toDouble(), tone.toDouble())
        }
    }

    val currentHex by remember {
        derivedStateOf {
            val argbLong = (currentHct.toInt().toUInt() or 0xFF000000u).toLong()
            ColorHelpers.longToHex(argbLong)
        }
    }

    val previewBgColors by remember {
        derivedStateOf {
            val hexList = if (isEditingBackground) {
                backgroundColorsHex.toMutableList().also { list ->
                    if (activeColorIndex in list.indices) list[activeColorIndex] = currentHex
                }
            } else backgroundColorsHex

            hexList.mapNotNull { ColorHelpers.hexToLong(it)?.let { c -> Color(c) } }
        }
    }

    val previewTextColors by remember {
        derivedStateOf {
            val hexList = if (!isEditingBackground) {
                textColorsHex.toMutableList().also { list ->
                    if (activeColorIndex in list.indices) list[activeColorIndex] = currentHex
                }
            } else textColorsHex

            hexList.mapNotNull { ColorHelpers.hexToLong(it)?.let { c -> Color(c) } }
        }
    }

    val hueBrush = remember(chroma, tone) {
        val stops = (0..360 step 30).map { angle ->
            Color(Hct.from(angle.toDouble(), chroma.toDouble(), tone.toDouble()).toInt())
        }
        Brush.horizontalGradient(stops)
    }

    val chromaBrush = remember(hue, tone) {
        val startColor = Color(Hct.from(hue.toDouble(), 0.0, tone.toDouble()).toInt())
        val midColor = Color(Hct.from(hue.toDouble(), 60.0, tone.toDouble()).toInt())
        val endColor = Color(Hct.from(hue.toDouble(), 120.0, tone.toDouble()).toInt())
        Brush.horizontalGradient(listOf(startColor, midColor, endColor))
    }

    val toneBrush = remember(hue, chroma) {
        val black = Color(Hct.from(hue.toDouble(), chroma.toDouble(), 0.0).toInt())
        val mid = Color(Hct.from(hue.toDouble(), chroma.toDouble(), 50.0).toInt())
        val white = Color(Hct.from(hue.toDouble(), chroma.toDouble(), 100.0).toInt())
        Brush.horizontalGradient(listOf(black, mid, white))
    }

    fun dismissWithAction(action: () -> Unit) {
        coroutineScope.launch {
            sheetState.hide()
            action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth(),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle(
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(bottom = MaterialTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = MaterialTheme.spacing.small)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            if (previewBgColors.isNotEmpty() && previewTextColors.isNotEmpty()) {
                ClickableLabelChip(
                    onClick = {},
                    title = labelTitle,
                    backgroundColors = previewBgColors,
                    textColors = previewTextColors
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            ColorSlider(
                label = stringResource(R.string.label_color_hue),
                valueText = "${hue.roundToInt()}°",
                value = hue,
                onValueChange = { hue = it },
                valueRange = 0f..360f,
                gradientBrush = hueBrush
            )

            ColorSlider(
                label = stringResource(R.string.label_color_chroma),
                valueText = "${chroma.roundToInt()}",
                value = chroma,
                onValueChange = { chroma = it },
                valueRange = 0f..120f,
                gradientBrush = chromaBrush
            )

            ColorSlider(
                label = stringResource(R.string.label_color_tone),
                valueText = "${tone.roundToInt()}%",
                value = tone,
                onValueChange = { tone = it },
                valueRange = 0f..100f,
                gradientBrush = toneBrush
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { dismissWithAction(onDismiss) }) {
                    Text(
                        stringResource(R.string.cancel_text),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                TextButton(onClick = { dismissWithAction { onApply(currentHex) } }) {
                    Text(
                        stringResource(R.string.ok_text),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}