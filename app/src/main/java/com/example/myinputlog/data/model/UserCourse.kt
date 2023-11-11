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
    val goalInHours: Long = 0L,
    val otherSourceHours: Long = 0L,
    val videos: List<YouTubeVideo> = listOf()
)