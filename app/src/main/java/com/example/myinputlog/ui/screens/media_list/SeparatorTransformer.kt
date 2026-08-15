package com.example.myinputlog.ui.screens.media_list

import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.screens.common.UiText
import com.example.myinputlog.ui.screens.common.ext.toLocalDate
import com.example.myinputlog.ui.screens.common.formatters.RelativeDateFormatter

class SeparatorTransformer(
    private val dateFormatter: RelativeDateFormatter = RelativeDateFormatter()
) {
    /**
     * Calculates if a separator is needed between two [VideoUiModel]s.
     * Returns a [UiText] if required, or null if no separator is needed.
     */
    fun getHeaderTitle(
        before: VideoUiModel?, after: VideoUiModel?, sort: SortOptions
    ): UiText? {
        if (after == null) return null

        return when (sort) {
            in SortOptions.videoDateSortOptions() -> {
                val beforeDate = before?.watchedOn?.toLocalDate()
                val afterDate = after.watchedOn.toLocalDate()

                if (beforeDate != afterDate) {
                    dateFormatter.format(afterDate)
                } else null
            }

            in SortOptions.videoChannelSortOptions() -> {
                val beforeChannel = before?.channelTitle
                val afterChannel = after.channelTitle
                if (beforeChannel != afterChannel) {
                    UiText.DynamicString(afterChannel)
                } else null
            }

            in SortOptions.videoTitleSortOptions() -> {
                val beforeTitle = before?.firstLetter
                val afterTitle = after.firstLetter
                if (beforeTitle != afterTitle) {
                    UiText.DynamicString(afterTitle)
                } else null
            }

            else -> null
        }
    }
}