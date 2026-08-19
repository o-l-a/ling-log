package com.example.myinputlog.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
data class LabelEntity(
    @PrimaryKey val id: String,
    val title: String,
    val color: Long = 0xFFFFC0CB,
    val secondaryColors: List<Long> = emptyList(),
    val textColor: Long = 0xFF000000,
    val secondaryTextColors: List<Long> = emptyList(),
    val isDeleted: Boolean = false,
    val lastUpdated: Long,
    val lastSynced: Long = 0L
)