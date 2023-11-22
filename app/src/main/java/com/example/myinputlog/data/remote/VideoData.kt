package com.example.myinputlog.data.remote

import com.example.myinputlog.data.model.YouTubeVideo
import kotlinx.serialization.Serializable
import java.time.Duration

@Serializable
data class VideoData(
    val items: List<VideoItem>
)

@Serializable
data class VideoItem(
    val id: String,
    val snippet: VideoSnippet,
    val contentDetails: VideoContentDetails
)

@Serializable
data class VideoSnippet(
    val title: String,
    val thumbnails: VideoThumbnails,
    val channelTitle: String,
    val defaultAudioLanguage: String? = null
)

@Serializable
data class VideoThumbnails(
    val default: VideoThumbnail,
    val medium: VideoThumbnail,
    val high: VideoThumbnail
)

@Serializable
data class VideoThumbnail(
    val url: String
)

@Serializable
data class VideoContentDetails(
    val duration: String
)

fun VideoData.toYouTubeVideo(): YouTubeVideo? {
    if (items.isNotEmpty()) {
        val item = items[0]
        return YouTubeVideo(
            title = item.snippet.title,
            thumbnailDefaultUrl = item.snippet.thumbnails.default.url,
            thumbnailMediumUrl = item.snippet.thumbnails.medium.url,
            thumbnailHighUrl = item.snippet.thumbnails.high.url,
            defaultAudioLanguage = item.snippet.defaultAudioLanguage ?: "",
            channel = item.snippet.channelTitle,
            durationInSeconds = Duration.parse(item.contentDetails.duration).seconds
        )
    }
    return null
}