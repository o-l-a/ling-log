package com.example.myinputlog.data.service.module

import android.content.Context
import android.net.Uri
import com.example.myinputlog.ui.screens.utils.AuthConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import javax.inject.Singleton
import androidx.core.net.toUri

@InstallIn(SingletonComponent::class)
@Module
object AuthModule {
    @Singleton
    @Provides
    fun provideAuthorizationService(@ApplicationContext context: Context): AuthorizationService =
        AuthorizationService(
            getApplication(context),
            AppAuthConfiguration.Builder()
                .setBrowserMatcher(
                    BrowserAllowList(
                        VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
                        VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
                    )
                ).build()
        )

    @Singleton
    @Provides
    fun provideAuthorizationServiceConfiguration(): AuthorizationServiceConfiguration =
        AuthorizationServiceConfiguration(
            AuthConstants.URL_AUTHORIZATION.toUri(),
            AuthConstants.URL_TOKEN_EXCHANGE.toUri(),
            null,
            AuthConstants.URL_LOGOUT.toUri()
        )
}