package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
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
fun FilterCardBackground(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val topRadius by animateDpAsState(
        targetValue = if (isFirst) MaterialTheme.spacing.medium else MaterialTheme.spacing.default,
        label = "top_corner"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (isLast) MaterialTheme.spacing.medium else MaterialTheme.spacing.default,
        label = "bottom_corner"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = topRadius, topEnd = topRadius,
            bottomStart = bottomRadius, bottomEnd = bottomRadius
        ),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        content = content
    )
}

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
            .padding(
                start = MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall,
                end = MaterialTheme.spacing.extraSmall
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onHeaderClick) {
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                })
        }
    }
}

@Composable
fun FilterItemRow(
    filter: FilterValueUiModel, onCheckedChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall,
                end = MaterialTheme.spacing.extraSmall
            ),
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
    item(key = "header_$title") {
        FilterCardBackground(
            isFirst = true,
            isLast = !isExpanded || items.isEmpty(),
            modifier = Modifier.animateItem()
        ) {
            FilterAreaHeader(title, isExpanded, onHeaderClick)
        }
    }

    if (isExpanded) {
        itemsIndexed(items, key = { _, it -> key(it) }) { index, item ->
            FilterCardBackground(
                isFirst = false,
                isLast = index == items.lastIndex,
                modifier = Modifier.animateItem()
            ) {
                itemContent(item)
            }
        }
    }

    item(key = "spacer_$title") { Spacer(Modifier.height(MaterialTheme.spacing.small)) }
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
    item(key = "header_$title") {
        FilterCardBackground(
            isFirst = true,
            isLast = !isExpanded || pagingItems.itemCount == 0,
            modifier = Modifier.animateItem()
        ) {
            FilterAreaHeader(title, isExpanded, onHeaderClick)
        }
    }

    if (isExpanded) {
        items(
            count = pagingItems.itemCount, key = pagingItems.itemKey { key(it) }) { index ->
            val item = pagingItems[index]

            val isLastItem = index == pagingItems.itemCount - 1
            val isLoadingMore = pagingItems.loadState.append is LoadState.Loading
            val isLast = isLastItem && !isLoadingMore

            FilterCardBackground(
                isFirst = false, isLast = isLast, modifier = Modifier.animateItem()
            ) {
                if (item != null) {
                    itemContent(item)
                }
            }
        }

        if (pagingItems.loadState.append is LoadState.Loading) {
            item(key = "loading_$title") {
                FilterCardBackground(
                    isFirst = false, isLast = true, modifier = Modifier.animateItem()
                ) {
                    Box(modifier = Modifier.padding(MaterialTheme.spacing.extraSmall)) {
                        LoadingBox()
                    }
                }
            }
        }
    }
}