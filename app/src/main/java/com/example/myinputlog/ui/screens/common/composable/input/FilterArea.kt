package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myinputlog.ui.models.FilterContentType
import com.example.myinputlog.ui.models.FilterValueUiModel
import com.example.myinputlog.ui.screens.common.composable.label.ClickableLabelChip
import com.example.myinputlog.ui.screens.common.composable.state.LoadingBox
import com.example.myinputlog.ui.theme.spacing


@Composable
fun FilterAreaHeader(
    title: String, isExpanded: Boolean, onHeaderClick: () -> Unit, modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f, label = "chevron_rotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.spacing.small))
            .clickable { onHeaderClick() }
            .padding(
                vertical = MaterialTheme.spacing.small,
                horizontal = MaterialTheme.spacing.extraSmall
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer {
                rotationZ = rotation
            })
    }
}

@Composable
fun FilterItemRow(
    filter: FilterValueUiModel, onCheckedChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        when (val content = filter.content) {
            is FilterContentType.Basic -> {
                Text(content.text)
            }

            is FilterContentType.Labeled -> {
                ClickableLabelChip(
                    title = content.text,
                    backgroundColor = content.colorRes,
                    textColor = content.textColorRes,
                    onClick = {})
            }
        }

        Checkbox(
            onCheckedChange = { onCheckedChange(filter.id) }, checked = filter.selected
        )
    }
}

/**
 * Overload for standard list
 */
inline fun <T : Any> LazyListScope.filterArea(
    title: String,
    items: List<T>,
    isExpanded: Boolean,
    noinline onHeaderClick: () -> Unit,
    crossinline key: (T) -> Any,
    crossinline itemContent: @Composable (item: T) -> Unit
) {
    item {
        FilterAreaHeader(title, isExpanded, onHeaderClick)
    }

    if (isExpanded) {
        items(
            items = items, key = { key(it) }) { item ->
            itemContent(item)
        }
    }
}

/**
 * Overload for paging items
 */
fun <T : Any> LazyListScope.filterArea(
    title: String,
    pagingItems: LazyPagingItems<T>,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    key: (T) -> Any,
    itemContent: @Composable (item: T) -> Unit
) {
    item {
        FilterAreaHeader(title, isExpanded, onHeaderClick)
    }

    if (isExpanded) {
        items(
            count = pagingItems.itemCount, key = pagingItems.itemKey { key(it) }) { index ->
            val item = pagingItems[index]
            if (item != null) {
                itemContent(item)
            }
        }

        if (pagingItems.loadState.append is LoadState.Loading) {
            item {
                LoadingBox()
            }
        }
    }
}