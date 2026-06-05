package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ChannelDto(
    val id: String? = null,
    val courseId: String? = null,
    val title: String? = null,
    val customUrl: String? = null,
    val country: String? = null,
    val thumbnailDefaultUrl: String? = null,
    val thumbnailMediumUrl: String? = null,
    val thumbnailHighUrl: String? = null,
    @get:PropertyName("isDeleted")
    val isDeleted: Boolean? = null,
    val lastUpdated: Timestamp? = null,
    val labelIds: List<String>? = null
)