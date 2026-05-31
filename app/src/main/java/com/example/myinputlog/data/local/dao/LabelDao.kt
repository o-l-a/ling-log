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
    @Query("SELECT * FROM user_labels ORDER BY title ASC")
    fun getLabelsFlow(): Flow<List<UserLabel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(labels: List<UserLabel>)

    @Query("SELECT MAX(timestamp) FROM user_labels")
    suspend fun getLatestTimestamp(): Timestamp?

    @Query(
        """
        UPDATE user_labels 
        SET totalTimeInSeconds = totalTimeInSeconds + :timeDelta, 
            totalVideoCount = totalVideoCount + :countDelta,
            timestamp = :timestamp
        WHERE id = :labelId
    """
    )
    suspend fun incrementStats(
        labelId: String, timeDelta: Long, countDelta: Long, timestamp: Timestamp
    )
}