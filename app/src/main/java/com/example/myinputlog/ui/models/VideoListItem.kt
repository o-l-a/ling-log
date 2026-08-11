package com.example.myinputlog.ui.models

import com.example.myinputlog.ui.screens.common.UiText

sealed interface VideoListItem {
    data class Video(val video: VideoUiModel) : VideoListItem
    data class Separator(val title: UiText) : VideoListItem
}