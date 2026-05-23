package com.example.myinputlog.data.model

import com.example.myinputlog.data.utils.TimestampSerializer
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class YouTubeChannel(
    @DocumentId val id: String = "",
    val title: String = "",
    @Serializable(with = TimestampSerializer::class)
    @ServerTimestamp
    val timestamp: Timestamp = Timestamp.now(),
    val customUrl: String? = null,
    val country: String? = null,
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultLabelIds: List<String> = emptyList(),

    // stats
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)
