package com.example.myinputlog.data.model

import com.example.myinputlog.data.utils.TimestampSerializer
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

data class UserCourse (
    @DocumentId
    val id: String = "",
    @ServerTimestamp
    val timestamp: Date = Date(),
    @Serializable(with = TimestampSerializer::class)
    @ServerTimestamp
    val lastUpdated: Timestamp = Timestamp.now(),
    @Serializable(with = TimestampSerializer::class)
    @ServerTimestamp
    val labelsLastUpdated: Timestamp = Timestamp.now(),
    val name: String = "",
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,

    // stats
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L,
    val totalActiveDays: Long = 0L
)