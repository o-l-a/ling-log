package com.example.myinputlog.ui.screens.video

import com.example.myinputlog.ui.models.CountryUiModel
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import java.util.Date
import kotlin.Long


sealed interface VideoLoadState {
    data object LoadingFromStorage : VideoLoadState
    data object Success : VideoLoadState
    data object StorageError : VideoLoadState
    data object NetworkError : VideoLoadState
    data object MetadataError : VideoLoadState
}

data class VideoForm(
    val id: String = "",
    val videoId: String = "",
    val videoUrl: String = "",
    val title: String = "",
    val watchedOn: Date = Date(),
    val speakersNationality: CountryUiModel? = null,
    val durationInSeconds: Long = 0L,
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val defaultAudioLanguage: String = "",
    // Channel Data
    val channelId: String = "",
    val channelTitle: String = "",
    val channelCustomUrl: String? = "",
    val channelCountry: String? = "",
    val channelThumbnailDefaultUrl: String = "",
    val channelThumbnailMediumUrl: String = "",
    val channelThumbnailHighUrl: String = "",
    // Labels
    val initialLabels: Set<LabelUiModel> = emptySet(),
    val allLabels: Set<LabelUiModel> = emptySet(),
    // Selection
    val searchQuery: String = "",
    val selectedCourse: CourseUiModel = CourseUiModel(),
    val selectedLabels: Set<LabelUiModel> = emptySet(),
    val saveLabelsForChannel: Boolean = false
)

data class VideoUiFlags(
    val isDeleteDialogVisible: Boolean = false,
    val isDatePickerDialogVisible: Boolean = false,
    val isEditStarted: Boolean = false,
    val isNewChannel: Boolean = false,
    val isDeleting: Boolean = false
)

sealed interface VideoUiState {
    data object Loading : VideoUiState
    data object Error : VideoUiState
    data class Success(
        val videoForm: VideoForm,
        val videoLoadState: VideoLoadState = VideoLoadState.LoadingFromStorage,
        val userCourses: List<CourseUiModel> = listOf(),
        val suggestions: Set<LabelUiModel> = emptySet(),
        val videoUiFlags: VideoUiFlags = VideoUiFlags(),
        val isFormValid: Boolean = false,
        val isDeleteEnabled: Boolean = false,
        val isSaveEnabled: Boolean = false,
        val isCourseEditable: Boolean = false
    ) : VideoUiState
}