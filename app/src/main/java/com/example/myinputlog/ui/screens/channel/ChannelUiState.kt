package com.example.myinputlog.ui.screens.channel

import com.example.myinputlog.ui.screens.video.ChannelMetadata


sealed interface ChannelUiState {
    data object Loading : ChannelUiState
    data object Error : ChannelUiState
    data class Success(
        val channelMetadata: ChannelMetadata = ChannelMetadata(),
        val channelLoadState: ChannelLoadState = ChannelLoadState.Loading,
        val isDeleteEnabled: Boolean = false,
    ) : ChannelUiState
}

sealed interface ChannelLoadState {
    data object Loading : ChannelLoadState
    data object Success : ChannelLoadState
    data object Error : ChannelLoadState
}