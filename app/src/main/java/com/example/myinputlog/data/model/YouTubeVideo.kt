package com.example.myinputlog.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class YouTubeVideo(
    @DocumentId
    val id: String = "",
    @ServerTimestamp
    val timestamp: Date = Date(),
    val watchedOn: Date = Date(),
    val speakersNationality: Int = 0,
    val title: String = "",
    val channel: String = "",
    val durationInSeconds: Long = 0L,
    val videoUrl: String = "",
    val thumbnailUrl: String = ""
)