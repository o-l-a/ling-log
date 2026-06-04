package com.example.myinputlog.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myinputlog.ui.screens.utils.Country
import java.util.Date

@Entity(
    tableName = "videos", foreignKeys = [ForeignKey(
        entity = ChannelEntity::class,
        parentColumns = ["id"],
        childColumns = ["channelId"],
        onDelete = ForeignKey.RESTRICT
    ), ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("channelId"), Index("courseId")]
)
data class VideoEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val courseId: String,
    val channelId: String,
    val watchedOn: Date,
    val speakersNationality: Country? = null,
    val title: String,
    val durationInSeconds: Long,
    val videoUrl: String,
    val thumbnailDefaultUrl: String,
    val thumbnailMediumUrl: String,
    val thumbnailHighUrl: String,
    val defaultAudioLanguage: String,
    val isDeleted: Boolean = false,
    val lastUpdated: Long,
    val lastSynced: Long = 0L
)