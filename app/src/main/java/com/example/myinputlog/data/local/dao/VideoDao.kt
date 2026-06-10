package com.example.myinputlog.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.entities.VideoLabelCrossRef
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import com.example.myinputlog.data.local.model.VideoWithLabelIds
import java.util.Date

@Dao
interface VideoDao {
    // GETS
    @Query("SELECT * FROM videos WHERE id = :videoId AND isDeleted = 0")
    suspend fun getVideoById(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE id IN (:ids)")
    suspend fun getVideosByIds(ids: List<String>): List<VideoEntity>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """SELECT DISTINCT 
    CAST(strftime('%s', date(watchedOn / 1000, 'unixepoch', 'start of month')) AS INTEGER) * 1000 
    FROM videos"""
    )
    suspend fun getAllUniqueMonthKeys(): List<Date>

    @Transaction
    @Query("SELECT * FROM videos WHERE id = :videoId AND isDeleted = 0")
    suspend fun getVideoWithChannelAndLabelsById(videoId: String): VideoWithChannelAndLabels?

    @Transaction
    @Query(
        """SELECT * FROM videos 
        WHERE isDeleted = 0 AND courseId = :courseId 
        ORDER BY watchedOn DESC, lastUpdated DESC"""
    )
    fun getVideosPagingSource(courseId: String): PagingSource<Int, VideoWithChannelAndLabels>

    // UPSERTS
    @Upsert
    suspend fun upsertVideo(video: VideoEntity)

    @Upsert
    suspend fun bulkUpsertVideos(videos: List<VideoEntity>)

    @Transaction
    suspend fun upsertVideoWithLabelIds(item: VideoWithLabelIds) {
        upsertVideo(item.video)
        deleteLabelRefsForVideo(item.video.id)
        val refs = item.labelIds.map { VideoLabelCrossRef(item.video.id, it) }
        insertVideoLabelRefs(refs)
    }

    @Transaction
    suspend fun bulkUpsertVideosWithLabelIds(items: List<VideoWithLabelIds>) {
        if (items.isEmpty()) return

        val videos = items.map { it.video }
        val videoIds = videos.map { it.id }

        bulkUpsertVideos(videos)

        videoIds.chunked(CHUNK_SIZE).forEach { chunk ->
            bulkDeleteLabelRefsForVideos(chunk)
        }

        val allRefs = items.flatMap { item ->
            item.labelIds.map { labelId ->
                VideoLabelCrossRef(videoId = item.video.id, labelId = labelId)
            }
        }

        if (allRefs.isNotEmpty()) {
            allRefs.chunked(CHUNK_SIZE).forEach { chunk ->
                insertVideoLabelRefs(chunk)
            }
        }
    }

    @Transaction
    suspend fun bulkUpsertVideosWithLabelIdsIfNewer(remoteData: List<VideoWithLabelIds>) {
        if (remoteData.isEmpty()) return

        val ids = remoteData.map { it.video.id }
        val localEntities = getVideosByIds(ids).associateBy { it.id }

        val toUpsert = remoteData.filter { remoteWrapper ->
            val remoteVideo = remoteWrapper.video
            val localVideo = localEntities[remoteVideo.id]
            localVideo == null || remoteVideo.lastUpdated > localVideo.lastUpdated
        }.map { remoteWrapper ->
            remoteWrapper.copy(
                video = remoteWrapper.video.copy(lastSynced = remoteWrapper.video.lastUpdated)
            )
        }

        if (toUpsert.isNotEmpty()) {
            bulkUpsertVideosWithLabelIds(toUpsert)
        }
    }

    // INSERTS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoLabelRefs(refs: List<VideoLabelCrossRef>)

    @Query(
        """
        INSERT OR IGNORE INTO video_label_cross_ref (videoId, labelId)
        SELECT id, :labelId 
        FROM videos 
        WHERE channelId = :channelId
    """
    )
    suspend fun insertLabelRefForChannelVideos(channelId: String, labelId: String)

    // UPDATES
    @Query("UPDATE videos SET lastUpdated = :timestamp WHERE channelId = :channelId")
    suspend fun updateVideosTimestampForChannel(channelId: String, timestamp: Long)

    @Transaction
    suspend fun syncLabelsToChannel(
        channelId: String,
        addedLabels: List<String>,
        removedLabels: List<String>,
        timestamp: Long = System.currentTimeMillis()
    ) {
        if (removedLabels.isNotEmpty()) {
            deleteVideoLabelRefsForChannel(channelId, removedLabels)
        }
        if (addedLabels.isNotEmpty()) {
            addedLabels.forEach { labelId ->
                insertLabelRefForChannelVideos(channelId, labelId)
            }
        }
        updateVideosTimestampForChannel(channelId, timestamp)
    }

    // DELETES
    @Query("UPDATE videos SET isDeleted = 1, lastUpdated = :timestamp WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM video_label_cross_ref WHERE videoId = :videoId")
    suspend fun deleteLabelRefsForVideo(videoId: String)

    @Query(
        """
        DELETE FROM video_label_cross_ref 
        WHERE labelId IN (:labelIds) 
        AND videoId IN (
            SELECT id 
            FROM videos 
            WHERE channelId = :channelId
        )
    """
    )
    suspend fun deleteVideoLabelRefsForChannel(channelId: String, labelIds: List<String>)

    @Query("DELETE FROM video_label_cross_ref WHERE videoId IN (:videoIds)")
    suspend fun bulkDeleteLabelRefsForVideos(videoIds: List<String>)

    // SYNC OPERATIONS
    @Transaction
    @Query("SELECT * FROM videos WHERE lastUpdated > lastSynced")
    suspend fun getUnsyncedVideosWithLabelIds(): List<VideoWithLabelIds>

    @Query("UPDATE videos SET lastSynced = lastUpdated WHERE id in (:ids)")
    suspend fun markVideosSynced(ids: List<String>)

    companion object {
        private const val CHUNK_SIZE = 999
    }
}