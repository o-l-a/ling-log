package com.example.myinputlog.data.service.module

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.paging.VideoPagingSource
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.screens.utils.MAX_PAGE_SIZE
import com.example.myinputlog.ui.screens.utils.PAGE_SIZE
import com.google.firebase.firestore.QuerySnapshot
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
        maxSize = MAX_PAGE_SIZE
    )

    @Provides
    @Singleton
    fun provideVideoPagingSource(
        storageService: DefaultStorageService,
        preferenceStorageService: DefaultPreferenceStorageService
    ) = VideoPagingSource(
        storageService = storageService,
        preferenceStorageService = preferenceStorageService
    )

    @Provides
    @Singleton
    fun provideVideoPager(
        pagingConfig: PagingConfig,
        videoPagingSource: VideoPagingSource
    ): Pager<QuerySnapshot, YouTubeVideo> {
        return Pager(
            config = pagingConfig
        ) {
            videoPagingSource
        }
    }
}