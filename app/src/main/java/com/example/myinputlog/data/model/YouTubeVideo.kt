package com.example.myinputlog.data.model

import com.example.myinputlog.ui.screens.utils.Country
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class YouTubeVideo(
    @DocumentId
    val id: String = "",
    @ServerTimestamp
    val timestamp: Date = Date(),
    val watchedOn: Date? = null,
    val speakersNationality: Country? = null,
    val title: String = "",
    val channel: String = "",
    val durationInSeconds: Long = 0L,
    val videoUrl: String = "",
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultAudioLanguage: String = ""
)