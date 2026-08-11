package com.example.myinputlog.ui.screens.common.composable.stats

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.myinputlog.ui.theme.spacing
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.DashedShape
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.time.LocalDate

internal val yearValueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
    val x = targets.first().x
    val year = LocalDate.ofEpochDay(x.toLong()).year
    "$year"
}

@Composable
internal fun rememberMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter = yearValueFormatter
): CartesianMarker {

    val label = rememberTextComponent(
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.labelMediumEmphasized.fontSize,
            fontWeight = MaterialTheme.typography.labelMediumEmphasized.fontWeight
        ),
        padding = Insets(MaterialTheme.spacing.small, MaterialTheme.spacing.extraSmall),
    )
    val guideline = rememberAxisGuidelineComponent(
        fill = Fill(MaterialTheme.colorScheme.secondary),
        thickness = MaterialTheme.spacing.extraExtraSmall,
        shape = DashedShape(
            dashLength = MaterialTheme.spacing.extraSmall,
            gapLength = MaterialTheme.spacing.extraExtraSmall
        )
    )
    return rememberDefaultCartesianMarker(
        label = label, valueFormatter = valueFormatter, guideline = guideline
    )
}