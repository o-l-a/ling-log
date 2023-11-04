package com.example.myinputlog.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserCourse (
    @DocumentId
    val id: String = "",
    @ServerTimestamp
    val timestamp: Date = Date(),
    val name: String = "",
    val goalInHours: Float = 0F,
    val otherSourceHours: Float = 0F,
    val hoursWatched: Float = 0F,
    val videos: List<YouTubeVideo> = listOf()
)