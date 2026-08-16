package com.example.myinputlog.data.local.query

import androidx.annotation.StringRes
import com.example.myinputlog.R

enum class SortOptions(@get:StringRes val optionName: Int) {
    DEFAULT(R.string.sort_default),
    WATCH_DATE_DESC(R.string.sort_watch_date_desc),
    WATCH_DATE_ASC(R.string.sort_watch_date_asc),
    LENGTH_DESC(R.string.sort_length_desc),
    LENGTH_ASC(R.string.sort_length_asc),
    TITLE_DESC(R.string.sort_title_desc),
    TITLE_ASC(R.string.sort_title_asc),
    CHANNEL_TITLE_DESC(R.string.sort_channel_title_desc),
    CHANNEL_TITLE_ASC(R.string.sort_channel_title_asc),
    VIDEO_COUNT_DESC(R.string.sort_video_count_desc),
    VIDEO_COUNT_ASC(R.string.sort_video_count_asc),
    TOTAL_TIME_DESC(R.string.sort_total_time_desc),
    TOTAL_TIME_ASC(R.string.sort_total_time_asc);

    companion object {
        fun videoSortOptions(): Set<SortOptions> = setOf(
            DEFAULT,
            WATCH_DATE_DESC,
            WATCH_DATE_ASC,
            LENGTH_DESC,
            LENGTH_ASC,
            TITLE_DESC,
            TITLE_ASC,
            CHANNEL_TITLE_DESC,
            CHANNEL_TITLE_ASC
        )

        fun channelSortOptions(): Set<SortOptions> = setOf(
            DEFAULT,
            CHANNEL_TITLE_DESC,
            CHANNEL_TITLE_ASC,
            VIDEO_COUNT_DESC,
            VIDEO_COUNT_ASC,
            TOTAL_TIME_DESC,
            TOTAL_TIME_ASC
        )

        fun videoDateSortOptions(): Set<SortOptions> = setOf(DEFAULT, WATCH_DATE_DESC, WATCH_DATE_ASC)

        fun videoChannelSortOptions(): Set<SortOptions> = setOf(CHANNEL_TITLE_ASC, CHANNEL_TITLE_DESC)

        fun videoTitleSortOptions(): Set<SortOptions> = setOf(TITLE_DESC, TITLE_ASC)
    }
}