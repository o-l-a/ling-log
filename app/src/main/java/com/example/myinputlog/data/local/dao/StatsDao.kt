package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {
    @Query(
        """
        SELECT l.title as labelName, SUM(v.durationInSeconds) as totalSeconds
        FROM labels l
        JOIN video_label_cross_ref ref ON l.id = ref.labelId
        JOIN videos v ON ref.videoId = v.id
        WHERE v.watchedOn BETWEEN :monthStart AND :monthEnd AND v.isDeleted = 0
        GROUP BY l.id
    """
    )
    fun getSecondsPerLabel(monthStart: Long, monthEnd: Long): Flow<List<LabelStat>>

    @Query(
        """
        SELECT CAST(watchedOn / 86400000 AS INTEGER) as day, COUNT(*) as count
        FROM videos
        WHERE isDeleted = 0
        GROUP BY day
    """
    )
    fun getVideoCountPerDay(): Flow<List<DayStat>>
}

data class LabelStat(val labelName: String, val totalSeconds: Long)
data class DayStat(val day: Int, val count: Int)