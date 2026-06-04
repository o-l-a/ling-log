package com.example.myinputlog.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ChannelData(
    val items: List<ChannelItem>
)

@Serializable
data class ChannelItem(
    val id: String, val snippet: ChannelSnippet
)

@Serializable
data class ChannelSnippet(
    val title: String,
    val customUrl: String? = null,
    val country: String? = null,
    val thumbnails: ChannelThumbnails
)

@Serializable
data class ChannelThumbnails(
    val default: ChannelThumbnail,
    val medium: ChannelThumbnail,
    val high: ChannelThumbnail
)

@Serializable
data class ChannelThumbnail(
    val url: String
)


fun ChannelItem.getChannelTitle(): String {
    return snippet.title
}