package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.ChannelLabelCrossRef

@Dao
interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChannel(channel: ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(channels: List<ChannelEntity>)

    @Query("SELECT * FROM channels WHERE id = :id AND isDeleted = 0")
    suspend fun getChannelById(id: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannelLabelRefs(refs: List<ChannelLabelCrossRef>)

    @Query("DELETE FROM channel_label_cross_ref WHERE channelId = :channelId")
    suspend fun deleteLabelsForChannel(channelId: String)

    @Transaction
    suspend fun upsertChannelWithLabels(channel: ChannelEntity, labelIds: List<String>) {
        upsertChannel(channel)
        deleteLabelsForChannel(channel.id)
        val refs = labelIds.map { ChannelLabelCrossRef(channel.id, it) }
        insertChannelLabelRefs(refs)
    }

    @Query("SELECT * FROM channels WHERE lastUpdated > lastSynced")
    suspend fun getUnsyncedChannels(): List<ChannelEntity>

    @Query("UPDATE channels SET lastSynced = :timestamp WHERE id = :id")
    suspend fun markSynced(id: String, timestamp: Long)
}