package com.example.myinputlog.data.model

import com.google.firebase.firestore.DocumentId

data class UserLabel(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val color: Int = 0xFFC0CB,

    // stats
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)