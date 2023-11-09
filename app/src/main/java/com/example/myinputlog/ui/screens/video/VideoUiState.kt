package com.example.myinputlog.ui.screens.video

import com.example.myinputlog.data.model.YouTubeVideo
import java.util.Date

data class VideoUiState(
    val id: String = "",
    val watchedOn: Date = Date(),
    val speakersNationality: Int = 0,
    val title: String = "",
    val channel: String = "",
    val durationInSeconds: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",

    val selectedCourseId: String = "",
    val isLoading: Boolean = true,
    val isEdit: Boolean = false,
    val isFormValid: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isDatePickerDialogVisible: Boolean = false
)

fun YouTubeVideo.toVideoUiState(
    selectedCourseId: String = "",
    isLoading: Boolean = true,
    isEdit: Boolean = false,
    isFormValid: Boolean = false,
    isDeleteDialogVisible: Boolean = false,
    isDatePickerDialogVisible: Boolean = false
): VideoUiState = VideoUiState(
    id = id,
    watchedOn = watchedOn,
    speakersNationality = speakersNationality,
    title = title,
    channel = channel,
    durationInSeconds = durationInSeconds.toString(),
    videoUrl = videoUrl,
    thumbnailUrl = thumbnailUrl,
    selectedCourseId = selectedCourseId,
    isLoading = isLoading,
    isEdit = isEdit,
    isFormValid = isFormValid,
    isDeleteDialogVisible = isDeleteDialogVisible,
    isDatePickerDialogVisible = isDatePickerDialogVisible
)

fun VideoUiState.toYouTubeVideo(): YouTubeVideo = YouTubeVideo(
    id = id,
    watchedOn = watchedOn,
    speakersNationality = speakersNationality,
    title = title,
    channel = channel,
    durationInSeconds = durationInSeconds.toLongOrNull() ?: 0L,
    videoUrl = videoUrl,
    thumbnailUrl = thumbnailUrl
)