package com.example.myinputlog.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val goalInHours: Long,
    val otherSourceHours: Long = 0L,
    val timestamp: Date,
    val isDeleted: Boolean = false,
    val lastUpdated: Long,
    val lastSynced: Long = 0L
)