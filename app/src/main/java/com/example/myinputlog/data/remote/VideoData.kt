package com.example.myinputlog.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class VideoData(
    val items: List<VideoItem>
)

@Serializable
data class VideoItem(
    val id: String, val snippet: VideoSnippet, val contentDetails: VideoContentDetails
)

@Serializable
data class VideoSnippet(
    val title: String,
    val thumbnails: VideoThumbnails,
    val channelId: String,
    val channelTitle: String,
    val defaultAudioLanguage: String? = null
)

@Serializable
data class VideoThumbnails(
    val default: VideoThumbnail, val medium: VideoThumbnail, val high: VideoThumbnail
)

@Serializable
data class VideoThumbnail(
    val url: String
)

@Serializable
data class VideoContentDetails(
    val duration: String
)


fun VideoItem.getChannelId(): String {
    return snippet.channelId
}