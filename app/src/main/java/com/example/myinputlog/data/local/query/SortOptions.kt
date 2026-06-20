package com.example.myinputlog.data.local.query

import androidx.annotation.StringRes
import com.example.myinputlog.R

enum class SortOptions(@get:StringRes val optionName: Int) {
    DEFAULT(R.string.sort_default),
    WATCH_DATE(R.string.sort_watch_date),
    TITLE(R.string.sort_title),
    CHANNEL_TITLE(R.string.sort_channel_title),
    VIDEO_COUNT(R.string.sort_video_count),
    TOTAL_TIME(R.string.sort_total_time)
}