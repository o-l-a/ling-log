package com.example.myinputlog.ui.screens.video

import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import com.example.myinputlog.data.remote.ChannelItem
import com.example.myinputlog.data.remote.VideoItem
import com.example.myinputlog.ui.models.toCountryUiModelOrNull
import com.example.myinputlog.ui.models.toLabelUiModel
import com.example.myinputlog.ui.screens.common.ext.stripUrl
import java.time.Duration
import java.util.UUID

fun VideoForm.toVideoEntity(): VideoEntity {
    return VideoEntity(
        id = id.ifBlank { UUID.randomUUID().toString() },
        videoId = videoId,
        courseId = selectedCourse.id,
        channelId = channelId,
        title = title,
        videoUrl = videoUrl.stripUrl(),
        watchedOn = watchedOn,
        speakersNationality = speakersNationality?.isoCode,
        durationInSeconds = durationInSeconds,
        thumbnailDefaultUrl = thumbnailDefaultUrl,
        thumbnailMediumUrl = thumbnailMediumUrl,
        thumbnailHighUrl = thumbnailHighUrl,
        defaultAudioLanguage = defaultAudioLanguage,
        lastUpdated = System.currentTimeMillis()
    )
}

fun VideoForm.toChannelEntity(): ChannelEntity {
    return ChannelEntity(
        id = channelId,
        courseId = selectedCourse.id,
        title = channelTitle,
        customUrl = channelCustomUrl,
        country = channelCountry,
        thumbnailDefaultUrl = channelThumbnailDefaultUrl,
        thumbnailMediumUrl = channelThumbnailMediumUrl,
        thumbnailHighUrl = channelThumbnailHighUrl,
        lastUpdated = System.currentTimeMillis()
    )
}

fun VideoForm.toClearedMetadata(): VideoForm {
    return this.copy(
        videoId = "",
        title = "",
        durationInSeconds = 0L,
        thumbnailDefaultUrl = "",
        thumbnailMediumUrl = "",
        thumbnailHighUrl = "",
        defaultAudioLanguage = "",
        speakersNationality = null,
        channelId = "",
        channelTitle = "",
        channelCustomUrl = "",
        channelCountry = "",
        channelThumbnailDefaultUrl = "",
        channelThumbnailMediumUrl = "",
        channelThumbnailHighUrl = "",
        selectedLabels = emptySet()
    )
}

fun VideoForm.toFormWithVideoMetadata(videoItem: VideoItem): VideoForm {
    return this.copy(
        videoId = videoItem.id,
        title = videoItem.snippet.title,
        durationInSeconds = Duration.parse(videoItem.contentDetails.duration).seconds,
        thumbnailDefaultUrl = videoItem.snippet.thumbnails.default.url,
        thumbnailMediumUrl = videoItem.snippet.thumbnails.medium.url,
        thumbnailHighUrl = videoItem.snippet.thumbnails.high.url,
        defaultAudioLanguage = videoItem.snippet.defaultAudioLanguage ?: "",
    )
}

fun VideoForm.toFormWithVideoMetadata(videoWithChannelAndLabels: VideoWithChannelAndLabels): VideoForm {
    return this.copy(
        id = videoWithChannelAndLabels.video.id,
        videoId = videoWithChannelAndLabels.video.videoId,
        title = videoWithChannelAndLabels.video.title,
        videoUrl = videoWithChannelAndLabels.video.videoUrl,
        speakersNationality = videoWithChannelAndLabels.video.speakersNationality?.toCountryUiModelOrNull(),
        initialSpeakersNationality = videoWithChannelAndLabels.video.speakersNationality?.toCountryUiModelOrNull(),
        watchedOn = videoWithChannelAndLabels.video.watchedOn,
        durationInSeconds = videoWithChannelAndLabels.video.durationInSeconds,
        thumbnailDefaultUrl = videoWithChannelAndLabels.video.thumbnailDefaultUrl,
        thumbnailMediumUrl = videoWithChannelAndLabels.video.thumbnailMediumUrl,
        thumbnailHighUrl = videoWithChannelAndLabels.video.thumbnailHighUrl,
        defaultAudioLanguage = videoWithChannelAndLabels.video.defaultAudioLanguage,
        initialLabels = videoWithChannelAndLabels.labels.map { it.toLabelUiModel() }.toSet(),
        selectedLabels = videoWithChannelAndLabels.labels.map { it.toLabelUiModel() }
            .toSortedSet(compareBy { it.title.lowercase() }),
    )
}

fun VideoForm.toFormWithChannelMetadata(channelItem: ChannelItem): VideoForm {
    return this.copy(
        channelId = channelItem.id,
        channelTitle = channelItem.snippet.title,
        channelCustomUrl = channelItem.snippet.customUrl,
        channelCountry = channelItem.snippet.country,
        channelThumbnailDefaultUrl = channelItem.snippet.thumbnails.default.url,
        channelThumbnailMediumUrl = channelItem.snippet.thumbnails.medium.url,
        channelThumbnailHighUrl = channelItem.snippet.thumbnails.high.url,
    )
}

fun VideoForm.toFormWithChannelMetadata(channelWithStatsAndLabels: ChannelWithStatsAndLabels): VideoForm {
    return this.copy(
        channelId = channelWithStatsAndLabels.channel.id,
        channelTitle = channelWithStatsAndLabels.channel.title,
        channelCustomUrl = channelWithStatsAndLabels.channel.customUrl,
        channelCountry = channelWithStatsAndLabels.channel.country,
        channelThumbnailDefaultUrl = channelWithStatsAndLabels.channel.thumbnailDefaultUrl,
        channelThumbnailMediumUrl = channelWithStatsAndLabels.channel.thumbnailMediumUrl,
        channelThumbnailHighUrl = channelWithStatsAndLabels.channel.thumbnailHighUrl,
    )
}