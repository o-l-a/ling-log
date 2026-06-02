package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myinputlog.data.local.entities.LabelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(label: LabelEntity)

    @Query("SELECT * FROM labels WHERE id = :labelId AND isDeleted = 0")
    suspend fun getLabelById(labelId: String): LabelEntity?

    @Query("SELECT * FROM labels WHERE isDeleted = 0")
    fun getAllLabels(courseId: String): Flow<List<LabelEntity>>
}