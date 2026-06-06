package com.example.myinputlog.ui.screens.label

import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.theme.ColorHelpers
import java.util.UUID

fun LabelForm.toLabelEntity(): LabelEntity = LabelEntity(
    id = id.ifBlank { UUID.randomUUID().toString() },
    title = title,
    textColor = previewTextColor ?: 0xFFFFC0CB,
    color = previewColor ?: 0xFF000000,
    lastUpdated = System.currentTimeMillis(),
)

fun LabelUiModel.toLabelForm(): LabelForm {
    return LabelForm(
        id = this.id,
        title = this.title,
        colorHex = ColorHelpers.longToHex(this.color),
        textColorHex = ColorHelpers.longToHex(this.textColor)
    )
}