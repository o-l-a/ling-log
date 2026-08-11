package com.example.myinputlog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.myinputlog.data.local.entities.CountryGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryGroupDao {
    // GETS
    @Query("SELECT * FROM country_groups WHERE id = :id")
    suspend fun getCountryGroupById(id: String): CountryGroupEntity?

    @Query("SELECT * FROM country_groups")
    fun getAllCountryGroups(): Flow<List<CountryGroupEntity>>

    // UPSERTS
    @Upsert
    suspend fun upsertCountryGroups(countryGroups: List<CountryGroupEntity>)
}