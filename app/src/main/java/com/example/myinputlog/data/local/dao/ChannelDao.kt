package com.example.myinputlog.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.ChannelLabelCrossRef
import com.example.myinputlog.data.local.model.ChannelWithLabelIds
import com.example.myinputlog.data.local.model.ChannelWithStatsAndLabels

@Dao
interface ChannelDao {
    // GETS
    @Query("SELECT * FROM channels WHERE id = :id AND isDeleted = 0")
    suspend fun getChannelById(id: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE id IN (:ids)")
    suspend fun getChannelsByIds(ids: List<String>): List<ChannelEntity>

    @Query("SELECT id FROM channels")
    suspend fun getAllIds(): List<String>

    @Transaction
    @Query(
        """SELECT 
            c.*, 
            COUNT(v.id) AS totalVideoCount, 
            COALESCE(SUM(v.durationInSeconds), 0) AS totalTimeInSeconds
        FROM channels c
        LEFT JOIN videos v ON c.id = v.channelId AND v.isDeleted = 0
        WHERE c.id = :id AND c.isDeleted = 0
        GROUP BY c.id"""
    )
    suspend fun getChannelWithLabelsById(id: String): ChannelWithStatsAndLabels?

    @Transaction
    @Query("""SELECT 
        c.*, 
        COUNT(v.id) AS totalVideoCount, 
        SUM(v.durationInSeconds) AS totalTimeInSeconds 
    FROM channels AS c
    LEFT JOIN videos AS v ON c.id = v.channelId
    WHERE c.courseId = :courseId
    GROUP BY c.id"""
    )
    fun getChannelsPagingSource(courseId: String): PagingSource<Int, ChannelWithStatsAndLabels>

    // UPSERTS
    @Upsert
    suspend fun upsertChannel(channel: ChannelEntity)

    @Upsert
    suspend fun bulkUpsertChannels(channels: List<ChannelEntity>)

    @Transaction
    suspend fun upsertChannelWithLabelIds(item: ChannelWithLabelIds) {
        upsertChannel(item.channel)
        deleteLabelRefsForChannel(item.channel.id)
        val refs = item.labelIds.map { ChannelLabelCrossRef(item.channel.id, it) }
        insertChannelLabelRefs(refs)
    }

    @Transaction
    suspend fun bulkUpsertChannelsWithLabelIds(items: List<ChannelWithLabelIds>) {
        if (items.isEmpty()) return

        val channels = items.map { it.channel }
        val channelIds = channels.map { it.id }

        bulkUpsertChannels(channels)

        channelIds.chunked(CHUNK_SIZE).forEach { chunk ->
            bulkDeleteLabelRefsForChannels(chunk)
        }

        val allRefs = items.flatMap { item ->
            item.labelIds.map { labelId ->
                ChannelLabelCrossRef(channelId = item.channel.id, labelId = labelId)
            }
        }

        if (allRefs.isNotEmpty()) {
            allRefs.chunked(CHUNK_SIZE).forEach { chunk ->
                insertChannelLabelRefs(chunk)
            }
        }
    }

    @Transaction
    suspend fun bulkUpsertChannelsWithLabelIdsIfNewer(remoteData: List<ChannelWithLabelIds>) {
        if (remoteData.isEmpty()) return

        val ids = remoteData.map { it.channel.id }
        val localEntities = getChannelsByIds(ids).associateBy { it.id }

        val toUpsert = remoteData.filter { remoteWrapper ->
            val remoteChannel = remoteWrapper.channel
            val localChannel = localEntities[remoteChannel.id]
            localChannel == null || remoteChannel.lastUpdated > localChannel.lastUpdated
        }.map { remoteWrapper ->
            remoteWrapper.copy(
                channel = remoteWrapper.channel.copy(lastSynced = remoteWrapper.channel.lastUpdated)
            )
        }

        if (toUpsert.isNotEmpty()) {
            bulkUpsertChannelsWithLabelIds(toUpsert)
        }
    }

    // INSERTS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannelLabelRefs(refs: List<ChannelLabelCrossRef>)

    // REF DELETES
    @Query("DELETE FROM channel_label_cross_ref WHERE channelId = :channelId")
    suspend fun deleteLabelRefsForChannel(channelId: String)

    @Query("DELETE FROM channel_label_cross_ref WHERE channelId IN (:channelIds)")
    suspend fun bulkDeleteLabelRefsForChannels(channelIds: List<String>)

    // SYNC OPERATIONS
    @Transaction
    @Query("SELECT * FROM channels WHERE lastUpdated > lastSynced")
    suspend fun getUnsyncedChannelsWithLabelIds(): List<ChannelWithLabelIds>

    @Query("UPDATE channels SET lastSynced = lastUpdated WHERE id IN (:ids)")
    suspend fun markChannelsSynced(ids: List<String>)

    companion object {
        private const val CHUNK_SIZE = 999
    }
}