package com.example.myinputlog.data.local.model

data class DailyWatchStat(
    val dateString: String, val totalSeconds: Long, val videoCount: Long
)

data class DailyStatRow(
    val dayOfMonth: String, val totalSeconds: Long, val videoCount: Long
)

data class RegionStat(
    val regionName: String?, val totalSeconds: Long
)