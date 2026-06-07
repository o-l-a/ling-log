package com.example.myinputlog.ui.screens.channel

import com.example.myinputlog.ui.models.LabelUiModel


sealed interface ChannelUiState {
    data object Loading : ChannelUiState
    data object Error : ChannelUiState
    data class Success(
        val channelLoadState: ChannelLoadState = ChannelLoadState.Loading,
        val form: ChannelForm = ChannelForm(),
        val metadata: ChannelMetadata = ChannelMetadata(),
        val suggestions: Set<LabelUiModel> = emptySet(),
        val uiFlags: ChannelUiFlags = ChannelUiFlags(),
    ) : ChannelUiState
}

sealed interface ChannelLoadState {
    data object Loading : ChannelLoadState
    data object Success : ChannelLoadState
    data object Error : ChannelLoadState
}

data class ChannelForm(
    val selectedLabels: Set<LabelUiModel> = emptySet(),
    val searchQuery: String = "",
    val syncLabelsToVideos: Boolean = false,
)

data class ChannelMetadata(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val customUrl: String? = null,
    val country: String? = null,
    val thumbnailDefaultUrl: String = "",
    val thumbnailMediumUrl: String = "",
    val thumbnailHighUrl: String = "",
    val initialLabels: Set<LabelUiModel> = emptySet(),
    val allLabels: Set<LabelUiModel> = emptySet(),
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)

data class ChannelUiFlags(
    val isDeleteEnabled: Boolean = false,
    val isFormValid: Boolean = false,
    val isEditStarted: Boolean = false,
    val isDialogVisible: Boolean = false
)