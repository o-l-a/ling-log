package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class VideoDto(
    val id: String? = null,
    val videoId: String? = null,
    val courseId: String? = null,
    val channelId: String? = null,
    val watchedOn: Timestamp? = null,
    val speakersNationality: String? = null,
    val title: String? = null,
    val durationInSeconds: Long? = null,
    val videoUrl: String? = null,
    val thumbnailDefaultUrl: String? = null,
    val thumbnailMediumUrl: String? = null,
    val thumbnailHighUrl: String? = null,
    val defaultAudioLanguage: String? = null,
    @get:PropertyName("isDeleted")
    val isDeleted: Boolean? = null,
    val lastUpdated: Timestamp? = null,
    val labelIds: List<String>? = null
)