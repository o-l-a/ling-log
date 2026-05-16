package com.example.myinputlog.data.service.module

import com.example.myinputlog.data.repository.VideoDataRepository
import com.example.myinputlog.data.repository.impl.DefaultVideoDataRepository
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.data.service.StorageService
import com.example.myinputlog.data.service.impl.DefaultAccountService
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun provideAccountService(impl: DefaultAccountService): AccountService

    @Binds
    abstract fun provideStorageService(impl: DefaultStorageService): StorageService

    @Binds
    abstract fun providePreferenceStorageService(impl: DefaultPreferenceStorageService): PreferenceStorageService

    @Binds
    abstract fun provideVideoDataRepository(impl: DefaultVideoDataRepository): VideoDataRepository
}