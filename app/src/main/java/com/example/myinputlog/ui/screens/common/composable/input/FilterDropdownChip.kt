package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterDropdownChip(
    selectedItem: T,
    items: List<T>,
    itemLabelMapper: @Composable (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val arrowRotationDegree by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "DropdownArrowRotation"
    )

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        FilterChip(
            modifier = Modifier.animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            ),
            selected = true,
            onClick = { expanded = !expanded },
            label = { Text(itemLabelMapper(selectedItem)) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null,
                    modifier = Modifier.rotate(arrowRotationDegree))
            })

        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(itemLabelMapper(item)) }, onClick = {
                    onItemSelected(item)
                    expanded = false
                })
            }
        }
    }
}