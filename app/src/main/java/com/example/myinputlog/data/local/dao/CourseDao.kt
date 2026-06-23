package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.model.CourseWithStats
import com.example.myinputlog.data.local.model.IsoCodesResult
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    // GETS
    @Transaction
    @Query(
        """SELECT 
            c.*, 
            COUNT(DISTINCT date(v.watchedOn / 1000, 'unixepoch')) as totalActiveDays,
            COUNT(v.id) AS totalVideoCount,
            COALESCE(SUM(v.durationInSeconds), 0) AS totalTimeInSeconds
        FROM courses c
        LEFT JOIN videos v ON c.id = v.courseId AND v.isDeleted = 0
        WHERE c.id = :id AND c.isDeleted = 0
        GROUP BY c.id"""
    )
    suspend fun getCourseById(id: String): CourseWithStats?

    @Query("SELECT * FROM courses WHERE id IN (:ids)")
    suspend fun getCoursesByIds(ids: List<String>): List<CourseEntity>

    @Transaction
    @Query(
        """SELECT 
            c.*, 
            COUNT(DISTINCT date(v.watchedOn / 1000, 'unixepoch')) as totalActiveDays,
            COUNT(v.id) AS totalVideoCount,
            COALESCE(SUM(v.durationInSeconds), 0) AS totalTimeInSeconds
        FROM courses c
        LEFT JOIN videos v ON c.id = v.courseId AND v.isDeleted = 0
        WHERE c.isDeleted = 0
        GROUP BY c.id"""
    )
    fun getAllCourses(): Flow<List<CourseWithStats>>

    @Query("SELECT id FROM courses")
    suspend fun getAllIds(): List<String>

    @Transaction
    @Query(
        """SELECT cg.isoCodes
        FROM courses AS c
        JOIN country_groups cg ON c.countryGroupId = cg.id
        WHERE c.id = :id
        """
    )
    suspend fun getAvailableCountries(id: String): IsoCodesResult?

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
    suspend fun bulkDeleteChannelsForCourse(
        courseId: String, timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE videos SET isDeleted = 1, lastUpdated = :timestamp WHERE courseId = :courseId")
    suspend fun bulkDeleteVideosForCourse(
        courseId: String, timestamp: Long = System.currentTimeMillis()
    )

    // SYNC OPERATIONS
    @Query("SELECT * FROM courses WHERE lastUpdated > lastSynced")
    suspend fun getUnsyncedCourses(): List<CourseEntity>

    @Query("UPDATE courses SET lastSynced = lastUpdated WHERE id IN (:ids)")
    suspend fun markCoursesSynced(ids: List<String>)
}