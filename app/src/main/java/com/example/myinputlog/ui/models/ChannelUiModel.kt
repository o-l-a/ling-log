package com.example.myinputlog.ui.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R
import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels
import com.example.myinputlog.ui.screens.common.formatDurationAsText

data class ChannelUiModel(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val customUrl: String? = null,
    val country: String? = null,
    val defaultLanguage: CountryUiModel? = null,
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultLabels: Set<LabelUiModel> = emptySet(),
    val rank: Int = 0,
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
) {
    @Composable
    fun supportingLine(): String =
        "${stringResource(R.string.channel_video_count)}: $totalVideoCount (${
            formatDurationAsText(
                totalTimeInSeconds
            )
        })"

    fun titleLines(): Int = if (defaultLabels.isNotEmpty()) 1 else 2
}

fun ChannelWithStatsAndLabels.toChannelUiModel(rank: Int = 0): ChannelUiModel = ChannelUiModel(
    id = channel.id,
    courseId = channel.courseId,
    title = channel.title,
    customUrl = channel.customUrl,
    country = channel.country,
    defaultLanguage = channel.defaultLanguage.toCountryUiModelOrNull(),
    thumbnailDefaultUrl = channel.thumbnailDefaultUrl,
    thumbnailMediumUrl = channel.thumbnailMediumUrl,
    thumbnailHighUrl = channel.thumbnailHighUrl,
    totalTimeInSeconds = totalTimeInSeconds,
    totalVideoCount = totalVideoCount,
    rank = rank,
    defaultLabels = labels.map { it.toLabelUiModel() }
        .toSortedSet(compareBy { it.title.lowercase() })
)