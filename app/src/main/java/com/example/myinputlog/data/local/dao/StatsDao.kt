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
        SELECT 
            strftime('%d', watchedOn / 1000, 'unixepoch') as dayOfMonth,
            SUM(durationInSeconds) as totalSeconds,
            COUNT(*) as videoCount
        FROM videos
        WHERE courseId = :courseId AND watchedOn BETWEEN :start AND :end AND isDeleted = 0
        GROUP BY dayOfMonth
    """
    )
    fun getDailyStats(courseId: String, start: Long, end: Long): Flow<List<DailyStatRow>>
}

data class LabelStat(val labelName: String, val totalSeconds: Long)

data class DailyStatRow(
    val dayOfMonth: String, val totalSeconds: Long, val videoCount: Long
)