package com.example.myinputlog.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSnippet(
    val title: String
)

@Serializable
data class PlaylistItem(
    val id: String,
    val snippet: PlaylistSnippet
)

@Serializable
data class PlaylistsData(
    val items: List<PlaylistItem>
)