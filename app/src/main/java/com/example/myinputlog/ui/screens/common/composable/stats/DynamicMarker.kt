package com.example.myinputlog.ui.screens.common.composable.stats

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.MarkerCornerBasedShape
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.util.Locale

@Composable
internal fun rememberDynamicMarker(
    bottomAxisValueFormatter: CartesianValueFormatter? = null,
    showIndicator: Boolean = true,
): CartesianMarker {
    val labelBackgroundShape = MarkerCornerBasedShape(RoundedCornerShape(8.dp), 0.dp)
    val labelBackground = rememberShapeComponent(
        fill = Fill(MaterialTheme.colorScheme.surfaceContainerHigh), shape = labelBackgroundShape
    )

    val label = rememberTextComponent(
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        padding = Insets(horizontal = 8.dp, vertical = 6.dp),
        margins = Insets(vertical = 24.dp),
        background = labelBackground,
        minWidth = TextComponent.MinWidth.fixed(48.dp),
    )

    val guideline = rememberAxisGuidelineComponent(
        fill = Fill.Transparent
    )

    val indicatorInnerDot = rememberShapeComponent(
        fill = Fill(MaterialTheme.colorScheme.surface), shape = CircleShape
    )

    val valueFormatter = remember(bottomAxisValueFormatter) {
        DefaultCartesianMarker.ValueFormatter { context, targets ->
            val lineTarget =
                targets.filterIsInstance<LineCartesianLayerMarkerTarget>().firstOrNull()
                    ?: return@ValueFormatter ""

            val y = lineTarget.points.firstOrNull()?.entry?.y ?: 0.0
            val yText = String.format(Locale.getDefault(), "%.2f%%", y)
            val xText = bottomAxisValueFormatter?.format(context, lineTarget.x, null)

            if (xText != null) "$xText · $yText" else yText
        }
    }

    return rememberDefaultCartesianMarker(
        label = label,
        labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
        valueFormatter = valueFormatter,
        indicator = if (showIndicator) {
            { color ->
                LayeredComponent(
                    back = ShapeComponent(Fill(color.copy(alpha = 0.20f)), CircleShape),
                    front = LayeredComponent(
                        back = ShapeComponent(fill = Fill(color), shape = CircleShape),
                        front = indicatorInnerDot,
                        padding = Insets(5.dp),
                    ),
                    padding = Insets(10.dp),
                )
            }
        } else {
            null
        },
        indicatorSize = 36.dp,
        guideline = guideline,
    )
}