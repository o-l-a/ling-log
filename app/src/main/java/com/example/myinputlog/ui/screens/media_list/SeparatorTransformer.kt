package com.example.myinputlog.ui.screens.media_list

import com.example.myinputlog.R
import com.example.myinputlog.data.local.query.SortOptions
import com.example.myinputlog.ui.models.VideoUiModel
import com.example.myinputlog.ui.screens.common.UiText
import com.example.myinputlog.ui.screens.common.ext.toLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class SeparatorTransformer(
    locale: Locale = Locale.getDefault(), private val today: LocalDate = LocalDate.now()
) {
    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    private val yesterday = today.minusDays(1)
    private val unixEpoch = LocalDate.of(1970, 1, 1)

    fun transform(before: VideoUiModel?, after: VideoUiModel?, sort: SortOptions): VideoUiModel? {
        if (after == null || after.isSeparator) return null

        return when (sort) {
            in SortOptions.videoDateSortOptions() -> {
                val beforeDate = before?.watchedOn?.toLocalDate()
                val afterDate = after.watchedOn.toLocalDate()

                if (beforeDate != afterDate) {
                    VideoUiModel(
                        separatorTitle = getWatchedOnHeader(afterDate), isSeparator = true
                    )
                } else null
            }

            in SortOptions.videoChannelSortOptions() -> {
                val beforeChannel = before?.channelTitle
                val afterChannel = after.channelTitle
                if (beforeChannel != afterChannel) {
                    VideoUiModel(
                        separatorTitle = UiText.DynamicString(afterChannel), isSeparator = true
                    )
                } else null
            }

            in SortOptions.videoTitleSortOptions() -> {
                val beforeTitle = before?.firstLetter
                val afterTitle = after.firstLetter
                if (beforeTitle != afterTitle) {
                    VideoUiModel(
                        separatorTitle = UiText.DynamicString(afterTitle), isSeparator = true
                    )
                } else null
            }

            else -> null
        }
    }

    fun getWatchedOnHeader(date: LocalDate, isNaturalText: Boolean = true): UiText {
        if (!isNaturalText) return UiText.DynamicString(date.format(formatter))
        return when (date) {
            today -> UiText.StringResource(R.string.today_text)
            yesterday -> UiText.StringResource(R.string.yesterday_text)
            unixEpoch -> UiText.StringResource(R.string.long_ago_text)
            else -> UiText.DynamicString(date.format(formatter))
        }
    }
}