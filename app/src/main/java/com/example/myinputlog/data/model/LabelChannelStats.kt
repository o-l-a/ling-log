package com.example.myinputlog.data.model

import com.google.firebase.firestore.DocumentId

data class LabelChannelStats(
    @DocumentId
    val id: String = "", // channel id
    val channelTitle: String = "",
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)
