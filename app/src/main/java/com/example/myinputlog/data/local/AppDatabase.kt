package com.example.myinputlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myinputlog.data.local.dao.LabelDao
import com.example.myinputlog.data.model.UserLabel

@Database(entities = [UserLabel::class], version = 1)
@TypeConverters(RoomTimestampConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labelDao(): LabelDao
}