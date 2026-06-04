package com.example.myinputlog.data.remote.dto

import com.example.myinputlog.ui.screens.utils.Country
import com.google.firebase.Timestamp

data class VideoDto(
    val id: String? = null,
    val courseId: String? = null,
    val channelId: String? = null,
    val watchedOn: Timestamp? = null,
    val speakersNationality: Country? = null,
    val title: String? = null,
    val durationInSeconds: Long? = null,
    val videoUrl: String? = null,
    val thumbnailDefaultUrl: String? = null,
    val thumbnailMediumUrl: String? = null,
    val thumbnailHighUrl: String? = null,
    val defaultAudioLanguage: String? = null,
    val isDeleted: Boolean? = null,
    val lastUpdated: Timestamp? = null,
    val labelIds: List<String>? = null
)