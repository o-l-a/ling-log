package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myinputlog.data.local.entities.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(course: CourseEntity)

    @Query("SELECT * FROM courses WHERE isDeleted = 0")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE lastUpdated > lastSynced")
    suspend fun getUnsyncedCourses(): List<CourseEntity>

    @Query("UPDATE courses SET lastSynced = :ts WHERE id = :id")
    suspend fun markSynced(id: String, ts: Long)
}