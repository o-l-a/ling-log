package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.entities.VideoLabelCrossRef
import com.example.myinputlog.data.local.model.VideoWithChannelAndLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideo(video: VideoEntity)

    @Transaction
    @Query("SELECT * FROM videos WHERE isDeleted = 0 ORDER BY watchedOn DESC")
    fun getAllVideosFullDetails(): Flow<List<VideoWithChannelAndLabels>>

    @Transaction
    @Query("SELECT * FROM videos WHERE id = :videoId AND isDeleted = 0")
    suspend fun getVideoById(videoId: String): VideoWithChannelAndLabels?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoLabelRefs(refs: List<VideoLabelCrossRef>)

    @Query("DELETE FROM video_label_cross_ref WHERE videoId = :videoId")
    suspend fun deleteLabelsForVideo(videoId: String)

    @Transaction
    suspend fun upsertVideoWithLabels(video: VideoEntity, labelIds: List<String>) {
        upsertVideo(video)
        deleteLabelsForVideo(video.id)
        val refs = labelIds.map { VideoLabelCrossRef(video.id, it) }
        insertVideoLabelRefs(refs)
    }

    @Query("SELECT * FROM videos WHERE lastUpdated > lastSynced")
    suspend fun getUnsyncedVideos(): List<VideoEntity>

    @Query("UPDATE videos SET lastSynced = :timestamp WHERE id = :id")
    suspend fun markSynced(id: String, timestamp: Long)
}