package com.example.myinputlog.data.service.module

import androidx.paging.PagingConfig
import com.example.myinputlog.ui.screens.utils.DEFAULT_INITIAL_PAGE_MULTIPLIER
import com.example.myinputlog.ui.screens.utils.MAX_PAGE_SIZE
import com.example.myinputlog.ui.screens.utils.PAGE_SIZE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PagingModule {
    @Provides
    @Singleton
    fun providePagingConfig() = PagingConfig(
        pageSize = PAGE_SIZE,
        maxSize = MAX_PAGE_SIZE,
        initialLoadSize = (PAGE_SIZE * DEFAULT_INITIAL_PAGE_MULTIPLIER).toInt()
    )
}