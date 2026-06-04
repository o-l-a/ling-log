package com.example.myinputlog.data.service.module

import com.example.myinputlog.data.local.AppDatabase
import com.example.myinputlog.data.local.dao.ChannelDao
import com.example.myinputlog.data.local.dao.CourseDao
import com.example.myinputlog.data.local.dao.LabelDao
import com.example.myinputlog.data.local.dao.StatsDao
import com.example.myinputlog.data.local.dao.VideoDao
import com.example.myinputlog.data.service.AppDatabaseManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(manager: AppDatabaseManager): AppDatabase = manager.getDatabase()

    @Provides
    fun provideLabelDao(database: AppDatabase): LabelDao {
        return database.labelDao()
    }

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    fun provideChannelDao(database: AppDatabase): ChannelDao {
        return database.channelDao()
    }

    @Provides
    fun provideCourseDao(database: AppDatabase): CourseDao {
        return database.courseDao()
    }

    @Provides
    fun provideStatsDao(database: AppDatabase): StatsDao {
        return database.statsDao()
    }
}
