package com.example.myinputlog.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels", foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("courseId")]
)
data class ChannelEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val customUrl: String? = null,
    val country: String? = null,
    val defaultLanguage: String? = null,
    val thumbnailDefaultUrl: String,
    val thumbnailMediumUrl: String,
    val thumbnailHighUrl: String,
    val isDeleted: Boolean = false,
    val lastUpdated: Long,
    val lastSynced: Long = 0L
)