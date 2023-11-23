package com.example.myinputlog.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistItemContentDetails(
    val videoId: String
)

@Serializable
data class PlaylistItemItem(
    val contentDetails: PlaylistItemContentDetails
)

@Serializable
data class PlaylistItemsData(
    val nextPageToken: String? = null,
    val items: List<PlaylistItemItem>
)
