package com.example.myinputlog.ui.screens.video

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class VideoUiState(
    @DocumentId
    val id: String = "",
    val watchedOn: Date = Date(),
    val speakersNationality: Int = 0,
    val title: String = "",
    val channel: String = "",
    val duration: Float = 0F,
    val link: String = "",

    val isLoading: Boolean = true,
    val isEdit: Boolean = false,
)
