package com.example.myinputlog.data.model

import com.google.firebase.firestore.DocumentId

data class UserMonthlyStats(
    @DocumentId val id: String = "", // Format: "2024-05"
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L,
    val days: Map<String, DayAggregation> = emptyMap()
)

data class DayAggregation(
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L,
    val labelBreakdown: Map<String, Long> = emptyMap(),
    val channelBreakdown: Map<String, Long> = emptyMap()
)