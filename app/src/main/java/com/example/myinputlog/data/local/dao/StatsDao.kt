package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels
import com.example.myinputlog.data.local.model.DailyStatRow
import com.example.myinputlog.data.local.model.DailyWatchStat
import com.example.myinputlog.data.local.model.LabelWithStats
import com.example.myinputlog.data.local.model.RegionStat
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {
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

    @Query(
        """
        SELECT 
            watchedOn / (1000 * 86400) as date,
            SUM(durationInSeconds) as totalSeconds,
            COUNT(*) as videoCount
        FROM videos
        WHERE courseId = :courseId AND (watchedOn / 1000.0) * 1000 BETWEEN :start AND :end AND isDeleted = 0
        GROUP BY date
        ORDER BY date ASC
    """
    )
    fun getDailyWatchStats(courseId: String, start: Long, end: Long): Flow<List<DailyWatchStat>>

    @Query(
        """
        SELECT 
        (
            SELECT COALESCE(SUM(v.durationInSeconds), 0)
            FROM videos AS v
            WHERE v.courseId = :courseId 
              AND v.watchedOn < :start 
              AND v.isDeleted = 0
        ) + (
            SELECT c.otherSourceHours * 3600
            FROM courses AS c
            WHERE c.id = :courseId
        )
    """
    )
    fun getBaselineProgress(courseId: String, start: Long): Flow<Long>

    @Query(
        """
        SELECT 
            c.defaultLanguage as regionName,
            SUM(v.durationInSeconds) as totalSeconds
        FROM videos v
        INNER JOIN channels c ON v.channelId = c.id
        WHERE v.courseId = :courseId 
          AND v.watchedOn BETWEEN :start AND :end 
          AND v.isDeleted = 0
        GROUP BY c.defaultLanguage
        ORDER BY totalSeconds DESC
    """
    )
    fun getRegionStats(courseId: String, start: Long, end: Long): Flow<List<RegionStat>>

    @Query(
        """
        SELECT 
            l.*, 
            SUM(v.durationInSeconds) as totalTimeInSeconds
        FROM labels l
        INNER JOIN video_label_cross_ref ref ON l.id = ref.labelId
        INNER JOIN videos v ON ref.videoId = v.id
        WHERE v.courseId = :courseId 
          AND v.watchedOn BETWEEN :start AND :end 
          AND v.isDeleted = 0
        GROUP BY l.id
        ORDER BY totalTimeInSeconds DESC
    """
    )
    fun getLabelStats(courseId: String, start: Long, end: Long): Flow<List<LabelWithStats>>

    @Transaction
    @Query(
        """
        SELECT 
            c.*, 
            SUM(v.durationInSeconds) as totalTimeInSeconds,
            COUNT(v.id) as totalVideoCount,
            MIN(v.watchedOn) as firstWatchedOn
        FROM channels c
        INNER JOIN videos v ON c.id = v.channelId
        WHERE v.courseId = :courseId 
          AND v.watchedOn BETWEEN :start AND :end 
          AND v.isDeleted = 0
          AND c.isDeleted = 0
        GROUP BY c.id
        ORDER BY COALESCE(SUM(v.durationInSeconds), 0) DESC, COUNT(v.id) DESC, c.title ASC
        LIMIT :limit
    """
    )
    fun getTopChannelsWithStatsAndLabels(
        courseId: String, start: Long, end: Long, limit: Int = 5
    ): Flow<List<ChannelWithStatsAndLabels>>
}
