package com.example.myinputlog.data.local.model

data class DailyWatchStat(
    val date: Long, val totalSeconds: Long, val videoCount: Long
)

data class DailyStatRow(
    val dayOfMonth: String, val totalSeconds: Long, val videoCount: Long
)

data class RegionStat(
    val regionName: String?, val totalSeconds: Long
)