package com.example.myinputlog.data.service.module

import com.example.myinputlog.data.utils.AndroidStringProvider
import com.example.myinputlog.data.utils.StringProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourceModule {
    @Binds
    @Singleton
    abstract fun bindStringProvider(impl: AndroidStringProvider): StringProvider
}