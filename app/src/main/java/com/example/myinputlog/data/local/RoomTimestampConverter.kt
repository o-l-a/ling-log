package com.example.myinputlog.data.local

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

class RoomTimestampConverter {
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }

    @TypeConverter
    fun toTimestamp(millis: Long?): Timestamp? {
        return millis?.let { Timestamp(Date(it)) }
    }
}