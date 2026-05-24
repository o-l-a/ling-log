package com.example.myinputlog.ui.screens.video

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.screens.utils.Country
import com.example.myinputlog.ui.screens.utils.ext.asStartOfDay
import com.example.myinputlog.ui.screens.utils.ext.stripUrl
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.Long


sealed interface VideoLoadState {
    data object LoadingFromStorage : VideoLoadState
    data object Success : VideoLoadState
    data object StorageError : VideoLoadState
    data object NetworkError : VideoLoadState
    data object MetadataError : VideoLoadState
}

data class VideoMetadata(
    val title: String = "",
    val channelMetadata: ChannelMetadata = ChannelMetadata(),
    val durationInSeconds: Long = 0L,
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultAudioLanguage: String = ""
)

data class ChannelMetadata(
    val id: String = "",
    val title: String = "",
    val customUrl: String? = "",
    val country: String? = "",
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultLabelIds: List<String> = emptyList(),
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)

data class VideoUserDraft(
    val selectedCourse: UserCourse = UserCourse(),
    val speakersNationality: Country? = null,
    val videoUrl: String = "",
    val watchedOn: Date? = Date.from(
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
    )
)

data class VideoUiFlags(
    val isDeleteDialogVisible: Boolean = false,
    val isDatePickerDialogVisible: Boolean = false,
    val isDeleting: Boolean = false
)

sealed interface VideoUiState {
    data object Loading : VideoUiState
    data object Error : VideoUiState
    data class Success(
        val id: String = "",
        val videoUserDraft: VideoUserDraft = VideoUserDraft(),
        val videoMetadata: VideoMetadata = VideoMetadata(),
        val videoLoadState: VideoLoadState = VideoLoadState.LoadingFromStorage,
        val userCourses: List<UserCourse> = listOf(),
        val videoUiFlags: VideoUiFlags = VideoUiFlags(),
        val isFormValid: Boolean = false,
        val isDeleteEnabled: Boolean = false,
        val isCourseEditable: Boolean = false
    ) : VideoUiState
}

fun YouTubeChannel.toChannelMetadata(): ChannelMetadata = ChannelMetadata(
    id = id,
    title = title,
    customUrl = customUrl,
    country = country,
    thumbnailDefaultUrl = thumbnailDefaultUrl,
    thumbnailMediumUrl = thumbnailMediumUrl,
    thumbnailHighUrl = thumbnailHighUrl,
    defaultLabelIds = defaultLabelIds,
    totalTimeInSeconds = totalTimeInSeconds,
    totalVideoCount = totalVideoCount
)

fun YouTubeVideo.toVideoMetadata(channelMetadata: ChannelMetadata): VideoMetadata = VideoMetadata(
    title = title,
    durationInSeconds = durationInSeconds,
    thumbnailDefaultUrl = thumbnailDefaultUrl,
    thumbnailMediumUrl = thumbnailMediumUrl,
    thumbnailHighUrl = thumbnailHighUrl,
    defaultAudioLanguage = defaultAudioLanguage,
    channelMetadata = channelMetadata
)

fun YouTubeVideo.toVideoUserDraft(selectedCourse: UserCourse): VideoUserDraft = VideoUserDraft(
    selectedCourse = selectedCourse,
    videoUrl = videoUrl,
    watchedOn = watchedOn,
    speakersNationality = speakersNationality
)

fun VideoUiState.Success.toYouTubeVideo(): YouTubeVideo = YouTubeVideo(
    id = id,
    watchedOn = videoUserDraft.watchedOn?.asStartOfDay() ?: Date(0),
    speakersNationality = videoUserDraft.speakersNationality,
    title = videoMetadata.title,
    channel = videoMetadata.channelMetadata.title,
    channelId = videoMetadata.channelMetadata.id,
    durationInSeconds = videoMetadata.durationInSeconds,
    videoUrl = videoUserDraft.videoUrl.stripUrl(),
    thumbnailDefaultUrl = videoMetadata.thumbnailDefaultUrl,
    thumbnailMediumUrl = videoMetadata.thumbnailMediumUrl,
    thumbnailHighUrl = videoMetadata.thumbnailHighUrl,
    defaultAudioLanguage = videoMetadata.defaultAudioLanguage,
    timestamp = Date()
)

fun VideoUiState.Success.toYouTubeChannel(): YouTubeChannel = YouTubeChannel(
    id = videoMetadata.channelMetadata.id,
    title = videoMetadata.channelMetadata.title,
    country = videoMetadata.channelMetadata.country,
    customUrl = videoMetadata.channelMetadata.customUrl,
    thumbnailHighUrl = videoMetadata.channelMetadata.thumbnailHighUrl,
    thumbnailMediumUrl = videoMetadata.channelMetadata.thumbnailMediumUrl,
    thumbnailDefaultUrl = videoMetadata.channelMetadata.thumbnailDefaultUrl,
    timestamp = Timestamp.now()
)