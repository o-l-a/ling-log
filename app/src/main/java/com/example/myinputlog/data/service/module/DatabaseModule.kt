package com.example.myinputlog.data.service.module

import android.content.Context
import androidx.room.Room
import com.example.myinputlog.data.local.AppDatabase
import com.example.myinputlog.data.local.dao.ChannelDao
import com.example.myinputlog.data.local.dao.CourseDao
import com.example.myinputlog.data.local.dao.LabelDao
import com.example.myinputlog.data.local.dao.VideoDao
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        return Room.databaseBuilder(
                context, AppDatabase::class.java, "user_db_$userId.db"
            ).fallbackToDestructiveMigration(true).build()
    }

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
}
