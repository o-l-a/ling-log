package com.example.myinputlog.data.local.model

import androidx.room.ColumnInfo

data class IsoCodesResult(
    @ColumnInfo(name = "isoCodes")
    val codes: List<String>
)