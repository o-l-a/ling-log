package com.example.myinputlog.ui.screens.common.composable.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.models.TrendsTimePeriod
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreenTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    periodOptions: List<TrendsTimePeriod>,
    selectedPeriod: TrendsTimePeriod,
    onPeriodChange: (period: TrendsTimePeriod) -> Unit
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    TopAppBar(scrollBehavior = scrollBehavior, modifier = modifier.drawWithContent {
        drawContent()
        val strokeWidth = 1.dp.toPx()
        val y = size.height - strokeWidth / 2
        drawLine(
            color = dividerColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
        )
    }, colors = colors, title = {
        LazyRow(
            modifier = Modifier.layout { measurable, constraints ->
                val offsetPx = 16.dp.roundToPx()
                val fullWidth = constraints.maxWidth + (offsetPx * 2)

                val placeable = measurable.measure(
                    Constraints.fixedWidth(fullWidth)
                )

                layout(constraints.maxWidth, placeable.height) {
                    placeable.placeRelative(-offsetPx, 0)
                }
            },
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.medium, end = MaterialTheme.spacing.mediumPlusPlus
            ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(periodOptions) { option ->
                val isSelected = selectedPeriod == option

                FilterChip(
                    selected = isSelected,
                    onClick = { onPeriodChange(option) },
                    label = { Text(text = stringResource(option.labelRes)) },
                    leadingIcon = if (isSelected) {
                        { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                    } else null)
            }
        }
    })
}