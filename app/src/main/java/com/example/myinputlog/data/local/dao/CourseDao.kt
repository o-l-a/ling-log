package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.myinputlog.data.local.entities.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    // GETS
    @Query("SELECT * FROM courses WHERE id = :id AND isDeleted = 0")
    suspend fun getCourseById(id: String): CourseEntity?

    @Query("SELECT * FROM courses WHERE id IN (:ids)")
    suspend fun getCoursesByIds(ids: List<String>): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE isDeleted = 0")
    fun getAllCourses(): Flow<List<CourseEntity>>

    // UPSERTS
    @Upsert
    suspend fun upsertCourse(course: CourseEntity)

    @Upsert
    suspend fun bulkUpsertCourses(courses: List<CourseEntity>)

    @Transaction
    suspend fun bulkUpsertCoursesIfNewer(remoteEntities: List<CourseEntity>) {
        if (remoteEntities.isEmpty()) return

        val ids = remoteEntities.map { it.id }
        val localEntities = getCoursesByIds(ids).associateBy { it.id }

        val toUpsert = remoteEntities.filter { remoteCourse ->
            val localCourse = localEntities[remoteCourse.id]
            localCourse == null || remoteCourse.lastUpdated > localCourse.lastUpdated
        }.map { remoteCourse ->
            remoteCourse.copy(lastSynced = remoteCourse.lastUpdated)
        }

        if (toUpsert.isNotEmpty()) {
            bulkUpsertCourses(toUpsert)
        }
    }

    // DELETE
    @Query("UPDATE courses SET isDeleted = 1, lastUpdated = :timestamp WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE channels SET isDeleted = 1, lastUpdated = :timestamp WHERE courseId = :courseId")
    suspend fun bulkDeleteChannelsForCourse(courseId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE videos SET isDeleted = 1, lastUpdated = :timestamp WHERE courseId = :courseId")
    suspend fun bulkDeleteVideosForCourse(courseId: String, timestamp: Long = System.currentTimeMillis())

    // SYNC OPERATIONS
    @Query("SELECT * FROM courses WHERE lastUpdated > lastSynced")
    suspend fun getUnsyncedCourses(): List<CourseEntity>

    @Query("UPDATE courses SET lastSynced = lastUpdated WHERE id IN (:ids)")
    suspend fun markCoursesSynced(ids: List<String>)
}