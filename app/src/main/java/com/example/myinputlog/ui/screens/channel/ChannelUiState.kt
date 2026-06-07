package com.example.myinputlog.ui.screens.channel

import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.LabelUiModel


sealed interface ChannelUiState {
    data object Loading : ChannelUiState
    data object Error : ChannelUiState
    data class Success(
        val channelUiModel: ChannelUiModel = ChannelUiModel(),
        val channelLoadState: ChannelLoadState = ChannelLoadState.Loading,
        val searchQuery: String,
        val suggestions: Set<LabelUiModel>,
        val isDeleteEnabled: Boolean = false,
    ) : ChannelUiState
}

sealed interface ChannelLoadState {
    data object Loading : ChannelLoadState
    data object Success : ChannelLoadState
    data object Error : ChannelLoadState
}