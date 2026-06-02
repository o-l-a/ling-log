package com.example.myinputlog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myinputlog.data.utils.TimestampSerializer
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_labels")
data class UserLabel(
    @DocumentId
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val courseId: String,
    val userId: String,
    val color: Long = 0xFFFFC0CB,
    val textColor: Long = 0xFF000000,
    @Serializable(with = TimestampSerializer::class)
    @ServerTimestamp
    val timestamp: Timestamp = Timestamp.now(),

    // stats
    val totalTimeInSeconds: Long = 0L,
    val totalVideoCount: Long = 0L
)