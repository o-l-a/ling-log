package com.example.myinputlog.data.model

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class YouTubeChannel(
    @DocumentId val id: String = "",
    val title: String = "",
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
