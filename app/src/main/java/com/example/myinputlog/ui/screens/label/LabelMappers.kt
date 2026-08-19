package com.example.myinputlog.ui.screens.label

import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.theme.ColorHelpers
import java.util.UUID

fun LabelForm.toLabelEntity(): LabelEntity {
    val bgLongs = previewColors.ifEmpty { listOf(0xFFFFC0CB) }
    val txtLongs = previewTextColors.ifEmpty { listOf(0xFF000000) }

    return LabelEntity(
        id = id.ifBlank { UUID.randomUUID().toString() },
        title = title,
        color = bgLongs.first(),
        secondaryColors = bgLongs.drop(1),
        textColor = txtLongs.first(),
        secondaryTextColors = txtLongs.drop(1),
        lastUpdated = System.currentTimeMillis()
    )
}

fun LabelUiModel.toLabelForm(): LabelForm = LabelForm(
    id = id,
    title = title,
    colorsHex = gradientColors.map { ColorHelpers.longToHex(it) },
    activeColorIndex = 0,
    textColorsHex = gradientTextColors.map { ColorHelpers.longToHex(it) },
    activeTextColorIndex = 0,
    autoCalculateTextColor = false
)