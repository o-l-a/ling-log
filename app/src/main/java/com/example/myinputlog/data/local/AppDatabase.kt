package com.example.myinputlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myinputlog.data.local.dao.ChannelDao
import com.example.myinputlog.data.local.dao.CountryGroupDao
import com.example.myinputlog.data.local.dao.CourseDao
import com.example.myinputlog.data.local.dao.LabelDao
import com.example.myinputlog.data.local.dao.StatsDao
import com.example.myinputlog.data.local.dao.VideoDao
import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.ChannelLabelCrossRef
import com.example.myinputlog.data.local.entities.CountryGroupEntity
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.entities.VideoLabelCrossRef

@Database(
    entities = [ChannelEntity::class, ChannelLabelCrossRef::class, CountryGroupEntity::class, CourseEntity::class, LabelEntity::class, VideoEntity::class, VideoLabelCrossRef::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labelDao(): LabelDao
    abstract fun videoDao(): VideoDao
    abstract fun channelDao(): ChannelDao
    abstract fun courseDao(): CourseDao
    abstract fun countryGroupDao(): CountryGroupDao
    abstract fun statsDao(): StatsDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE labels ADD COLUMN secondaryColors TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE labels ADD COLUMN secondaryTextColors TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}