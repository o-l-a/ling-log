package com.example.myinputlog.data.service.module

import com.example.myinputlog.ui.screens.common.formatters.RelativeDateFormatter
import com.example.myinputlog.ui.screens.media_list.SeparatorTransformer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TransformerModule {

    @Provides
    @Singleton
    fun provideRelativeDateFormatter(): RelativeDateFormatter {
        return RelativeDateFormatter()
    }

    @Provides
    fun provideSeparatorTransformer(
        dateFormatter: RelativeDateFormatter
    ): SeparatorTransformer {
        return SeparatorTransformer(dateFormatter)
    }
}