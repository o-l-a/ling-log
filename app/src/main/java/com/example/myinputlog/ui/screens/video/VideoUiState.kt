package com.example.myinputlog.ui.screens.video

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.ui.screens.utils.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

data class VideoUiState(
    val id: String = "",
    val watchedOn: Date? = null,
    val speakersNationality: Country? = null,
    val title: String = "",
    val channel: String = "",
    val durationInSeconds: String = "",
    val videoUrl: String = "",
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultAudioLanguage: String = "",

    val userCourses: Flow<List<UserCourse>?> = MutableStateFlow(null),
    val selectedCourseId: String = "",
    val isLoading: Boolean = true,
    val isEdit: Boolean = false,
    val isFormValid: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isDatePickerDialogVisible: Boolean = false,
    val networkError: Boolean = false
)

fun YouTubeVideo.toVideoUiState(
    selectedCourseId: String = "",
    isLoading: Boolean = true,
    isEdit: Boolean = false,
    isFormValid: Boolean = false,
    isDeleteDialogVisible: Boolean = false,
    isDatePickerDialogVisible: Boolean = false,
    networkError: Boolean = false
): VideoUiState = VideoUiState(
    id = id,
    watchedOn = watchedOn,
    speakersNationality = speakersNationality,
    title = title,
    channel = channel,
    durationInSeconds = durationInSeconds.toString(),
    videoUrl = videoUrl,
    thumbnailDefaultUrl = thumbnailDefaultUrl,
    thumbnailMediumUrl = thumbnailMediumUrl,
    thumbnailHighUrl = thumbnailHighUrl,
    defaultAudioLanguage = defaultAudioLanguage,
    selectedCourseId = selectedCourseId,
    isLoading = isLoading,
    isEdit = isEdit,
    isFormValid = isFormValid,
    isDeleteDialogVisible = isDeleteDialogVisible,
    isDatePickerDialogVisible = isDatePickerDialogVisible,
    networkError = networkError
)

fun VideoUiState.toYouTubeVideo(): YouTubeVideo = YouTubeVideo(
    id = id,
    watchedOn = watchedOn ?: Date(0),
    speakersNationality = speakersNationality,
    title = title,
    channel = channel,
    durationInSeconds = durationInSeconds.toLongOrNull() ?: 0L,
    videoUrl = videoUrl,
    thumbnailDefaultUrl = thumbnailDefaultUrl,
    thumbnailMediumUrl = thumbnailMediumUrl,
    thumbnailHighUrl = thumbnailHighUrl,
    defaultAudioLanguage = defaultAudioLanguage
)