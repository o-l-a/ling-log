package com.example.myinputlog.ui.screens.video

import com.example.myinputlog.data.model.YouTubeVideo
import com.google.firebase.firestore.DocumentId
import java.util.Date

data class VideoUiState(
    @DocumentId
    val id: String = "",
    val watchedOn: Date = Date(),
    val speakersNationality: Int = 0,
    val title: String = "",
    val channel: String = "",
    val duration: String = "",
    val link: String = "",

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
    duration = duration.toString(),
    link = link,
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
    duration = duration.toFloatOrNull() ?: 0F,
    link = link
)