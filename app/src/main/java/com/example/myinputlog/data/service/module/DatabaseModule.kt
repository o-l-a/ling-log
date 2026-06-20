package com.example.myinputlog.data.service.module

import android.content.Context
import com.example.myinputlog.data.service.AppDatabaseManager
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
    fun provideDatabaseManager(
        @ApplicationContext context: Context,
    ): AppDatabaseManager {
        return AppDatabaseManager(context)
    }
}
