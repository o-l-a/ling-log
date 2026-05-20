package com.example.myinputlog.data.remote

import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.ui.screens.video.ChannelMetadata
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

fun ChannelData.toYouTubeChannel(): YouTubeChannel? {
    if (items.isNotEmpty()) {
        val item = items[0]
        return YouTubeChannel(
            title = item.snippet.title,
            thumbnailDefaultUrl = item.snippet.thumbnails.default.url,
            thumbnailMediumUrl = item.snippet.thumbnails.medium.url,
            thumbnailHighUrl = item.snippet.thumbnails.high.url,
            id = item.id,
            customUrl = item.snippet.customUrl,
            country = item.snippet.country
        )
    }
    return null
}

fun ChannelData.toChannelMetadata(): ChannelMetadata? {
    if (items.isNotEmpty()) {
        val item = items[0]
        return ChannelMetadata(
            title = item.snippet.title,
            thumbnailDefaultUrl = item.snippet.thumbnails.default.url,
            thumbnailMediumUrl = item.snippet.thumbnails.medium.url,
            thumbnailHighUrl = item.snippet.thumbnails.high.url,
            id = item.id,
            customUrl = item.snippet.customUrl,
            country = item.snippet.country
        )
    }
    return null
}