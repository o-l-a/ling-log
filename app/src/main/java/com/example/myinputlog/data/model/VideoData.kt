package com.example.myinputlog.data.model

import android.os.Build
import androidx.annotation.RequiresApi
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
    val channelTitle: String
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
            id = item.id,
            title = item.snippet.title,
            thumbnailUrl = item.snippet.thumbnails.default.url,
            channel = item.snippet.channelTitle,
            durationInSeconds = Duration.parse(item.contentDetails.duration).seconds
        )
    }
    return null
}