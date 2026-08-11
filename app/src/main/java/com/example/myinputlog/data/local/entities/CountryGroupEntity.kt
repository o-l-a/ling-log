package com.example.myinputlog.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "country_groups")
data class CountryGroupEntity(
    @PrimaryKey val id: String,
    val nameKey: String,
    val fallbackName: String,
    val isoCodes: List<String>
)
