package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myinputlog.data.model.UserLabel
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Query("SELECT * FROM user_labels WHERE userId = :userId AND courseId = :courseId ORDER BY title ASC")
    fun getLabelsFlow(userId: String, courseId: String): Flow<List<UserLabel>>

    @Query("SELECT * FROM user_labels WHERE id = :labelId AND userId = :userId AND courseId = :courseId")
    fun getLabelById(userId: String, courseId: String, labelId: String): Flow<UserLabel?>

    @Query("DELETE FROM user_labels WHERE id = :labelId AND userId = :userId AND courseId = :courseId")
    suspend fun deleteLabelById(userId: String, courseId: String, labelId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(labels: List<UserLabel>)

    @Query("SELECT MAX(timestamp) FROM user_labels")
    suspend fun getLatestTimestamp(userId: String, courseId: String): Timestamp?

    @Query(
        """
        UPDATE user_labels 
        SET totalTimeInSeconds = totalTimeInSeconds + :timeDelta, 
            totalVideoCount = totalVideoCount + :countDelta,
            timestamp = :timestamp
        WHERE id = :labelId
        AND userId = :userId
        AND courseId = :courseId
    """
    )
    suspend fun incrementStats(
        userId: String,
        courseId: String,
        labelId: String,
        timeDelta: Long,
        countDelta: Long,
        timestamp: Timestamp
    )
}