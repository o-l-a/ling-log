package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.myinputlog.data.local.entities.LabelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    // GETS
    @Query("SELECT * FROM labels WHERE id = :id AND isDeleted = 0")
    suspend fun getLabelById(id: String): LabelEntity?

    @Query("SELECT * FROM labels WHERE id IN (:ids)")
    suspend fun getLabelsByIds(ids: List<String>): List<LabelEntity>

    @Query("SELECT * FROM labels WHERE isDeleted = 0 ORDER BY title ASC")
    fun getAllLabels(): Flow<List<LabelEntity>>

    @Query("SELECT * FROM labels")
    fun getAllLabelsAsList(): List<LabelEntity>

    @Query("SELECT * FROM labels WHERE isDeleted = 0 ORDER BY title ASC")
    fun getActiveLabelsAsList(): List<LabelEntity>

    // UPSERTS
    @Upsert
    suspend fun upsertLabel(label: LabelEntity)

    @Upsert
    suspend fun bulkUpsertLabels(labels: List<LabelEntity>)

    @Transaction
    suspend fun bulkUpsertLabelsIfNewer(remoteEntities: List<LabelEntity>) {
        if (remoteEntities.isEmpty()) return

        val ids = remoteEntities.map { it.id }
        val localEntities = getLabelsByIds(ids).associateBy { it.id }

        val toUpsert = remoteEntities.filter { remoteLabel ->
            val localLabel = localEntities[remoteLabel.id]
            localLabel == null || remoteLabel.lastUpdated > localLabel.lastUpdated
        }.map { remoteLabel ->
            remoteLabel.copy(lastSynced = remoteLabel.lastUpdated)
        }

        if (toUpsert.isNotEmpty()) {
            bulkUpsertLabels(toUpsert)
        }
    }

    // DELETES
    @Query("UPDATE labels SET isDeleted = 1, lastUpdated = :timestamp WHERE id = :labelId")
    suspend fun deleteLabelById(labelId: String, timestamp: Long = System.currentTimeMillis())

    // SYNC OPERATIONS
    @Query("SELECT * FROM labels WHERE lastUpdated > lastSynced")
    fun getUnsyncedLabels(): List<LabelEntity>

    @Query("UPDATE labels SET lastSynced = lastUpdated WHERE id IN (:ids)")
    suspend fun markLabelsSynced(ids: List<String>)
}