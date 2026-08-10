package com.example.myinputlog.ui.screens.common.composable.channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.screens.common.composable.label.SmallLabelChip
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmallChannelRepresentationRow(
    modifier: Modifier = Modifier,
    channels: List<ChannelUiModel>,
    extraItemCount: Int = 0
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides MaterialTheme.spacing.default) {
        ContextualFlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            itemVerticalAlignment = Alignment.CenterVertically,
            maxLines = 1,
            itemCount = channels.size,
            overflow = ContextualFlowRowOverflow.expandIndicator {
                val remaining = (channels.size + extraItemCount) - shownItemCount
                if (remaining > 0) {
                    SmallLabelChip(
                        modifier = Modifier.height(MaterialTheme.spacing.medium),
                        title = "+$remaining",
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }) { index ->
            val channel = channels.getOrNull(index)
            channel?.let {
                ChannelThumbnail(
                    modifier = Modifier.height(MaterialTheme.spacing.mediumPlusPlus),
                    channelThumbnailUrl = channel.thumbnailMediumUrl
                )
            }
        }
    }
}