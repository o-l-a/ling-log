package com.example.myinputlog.data.model

import kotlinx.serialization.Serializable


@Serializable
data class RelatedPlaylists(
    val likes: String
)

@Serializable
data class NestedContentDetails(
    val relatedPlaylists: RelatedPlaylists
)

@Serializable
data class ContentDetails(
    val contentDetails: NestedContentDetails
)

@Serializable
data class ChannelData(
    val items: List<ContentDetails>
)
