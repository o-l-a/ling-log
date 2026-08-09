package com.example.myinputlog.data.local.model

import androidx.room.Ignore

data class DailyWatchStat(
    val date: Long, val totalSeconds: Long, val videoCount: Long
)

data class DailyWatchWrapper(
    val dailyStats: List<DailyWatchStat>,
    val years: List<Long>
)

data class DailyStatRow(
    val dayOfMonth: String, val totalSeconds: Long, val videoCount: Long
)

data class RegionStat(
    val regionName: String,
    val totalSeconds: Long,
    @Ignore val channelBreakdown: List<ChannelContribution> = emptyList()
) {
    constructor(regionName: String, totalSeconds: Long) : this(
        regionName, totalSeconds, emptyList()
    )
}

data class ChannelContribution(
    val channelId: String?,
    val channelName: String,
    val thumbnailMediumUrl: String?,
    val totalSeconds: Long
)