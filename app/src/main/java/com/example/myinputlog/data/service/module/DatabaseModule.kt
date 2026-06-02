package com.example.myinputlog.data.service.module

import android.content.Context
import androidx.room.Room
import com.example.myinputlog.data.local.AppDatabase
import com.example.myinputlog.data.local.dao.LabelDao
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
        ).build()
    }

    @Provides
    fun provideLabelDao(database: AppDatabase): LabelDao {
        return database.labelDao()
    }
}
