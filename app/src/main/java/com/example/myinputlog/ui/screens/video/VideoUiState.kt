package com.example.myinputlog.ui.screens.video

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.screens.utils.Country
import com.example.myinputlog.ui.screens.utils.ext.asStartOfDay
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date


sealed interface VideoLoadState {
    data object LoadingFromStorage : VideoLoadState
    data object Success : VideoLoadState
    data object StorageError : VideoLoadState
    data object NetworkError : VideoLoadState
    data object MetadataError : VideoLoadState
}

data class VideoMetadata(
    val title: String = "",
    val channel: String = "",
    val durationInSeconds: Long = 0L,
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultAudioLanguage: String = ""
)

data class VideoUserDraft(
    val selectedCourseId: String = "",
    val speakersNationality: Country? = null,
    val videoUrl: String = "",
    val watchedOn: Date? = Date.from(
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
    )
)

data class VideoUiFlags(
    val isDeleteDialogVisible: Boolean = false,
    val isDatePickerDialogVisible: Boolean = false
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
        val isFormValid: Boolean = false
    ) : VideoUiState
}

fun YouTubeVideo.toVideoMetadata(): VideoMetadata = VideoMetadata(
    title = title,
    channel = channel,
    durationInSeconds = durationInSeconds,
    thumbnailDefaultUrl = thumbnailDefaultUrl,
    thumbnailMediumUrl = thumbnailMediumUrl,
    thumbnailHighUrl = thumbnailHighUrl,
    defaultAudioLanguage = defaultAudioLanguage
)

fun YouTubeVideo.toVideoUserDraft(selectedCourseId: String = ""): VideoUserDraft = VideoUserDraft(
    selectedCourseId = selectedCourseId,
    videoUrl = videoUrl,
    watchedOn = watchedOn,
    speakersNationality = speakersNationality
)

fun VideoUiState.Success.toYouTubeVideo(): YouTubeVideo = YouTubeVideo(
    id = id,
    watchedOn = videoUserDraft.watchedOn?.asStartOfDay() ?: Date(0),
    speakersNationality = videoUserDraft.speakersNationality,
    title = videoMetadata.title,
    channel = videoMetadata.channel,
    durationInSeconds = videoMetadata.durationInSeconds,
    videoUrl = videoUserDraft.videoUrl,
    thumbnailDefaultUrl = videoMetadata.thumbnailDefaultUrl,
    thumbnailMediumUrl = videoMetadata.thumbnailMediumUrl,
    thumbnailHighUrl = videoMetadata.thumbnailHighUrl,
    defaultAudioLanguage = videoMetadata.defaultAudioLanguage
)