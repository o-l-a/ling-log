package com.example.myinputlog.ui.models

import com.example.myinputlog.R
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import com.example.myinputlog.ui.screens.common.UiText
import java.util.Date

data class VideoUiModel(
    val id: String = "",
    val watchedOn: Date = Date(0),
    val speakersNationality: CountryUiModel? = null,
    val title: String = "",
    val channelTitle: String = "",
    val durationInSeconds: Long = 0L,
    val videoUrl: String = "",
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultAudioLanguage: String = "",
    val labels: Set<LabelUiModel> = emptySet(),
    val isSeparator: Boolean = false,
    val separatorTitle: UiText = UiText.StringResource(R.string.today_text)
) {
    fun supportingLine(): String =
        "${channelTitle}${if (speakersNationality != null) " • ${speakersNationality.flagEmoji}" else ""}"

    fun titleLines(): Int = if (labels.isNotEmpty()) 1 else 2

    val firstLetter: String get() = title.firstOrNull().toString().uppercase()
}

fun VideoWithChannelAndLabels.toVideoUiModel(): VideoUiModel = VideoUiModel(
    id = video.id,
    watchedOn = video.watchedOn,
    speakersNationality = video.speakersNationality?.toCountryUiModelOrNull(),
    title = video.title,
    channelTitle = channel.title,
    durationInSeconds = video.durationInSeconds,
    videoUrl = video.videoUrl,
    thumbnailDefaultUrl = video.thumbnailDefaultUrl,
    thumbnailMediumUrl = video.thumbnailMediumUrl,
    thumbnailHighUrl = video.thumbnailHighUrl,
    defaultAudioLanguage = video.defaultAudioLanguage,
    labels = labels.map { it.toLabelUiModel() }.toSortedSet(compareBy { it.title.lowercase() })
)