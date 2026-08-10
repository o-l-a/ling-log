package com.example.myinputlog.ui.models

data class MonthlyStatsUiModel(
    val id: String = "", // Format: "2024-05"
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L,
    val days: Map<String, DayAggregation> = emptyMap() // Key format: day_10
)

data class DayAggregation(
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)

data class TopItemsUiModel<T>(
    val items: List<T>,
    val extraItemCount: Long
)

data class MonthlyDashboardUiModel(
    val stats: MonthlyStatsUiModel,
    val topChannels: TopItemsUiModel<ChannelUiModel>,
    val topLabels: TopItemsUiModel<LabelUiModel>
)