package com.example.myinputlog.ui.screens.recently_watched

import androidx.paging.PagingData
import com.example.myinputlog.data.model.ChannelData
import com.example.myinputlog.data.model.PlaylistsData
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class RecentlyWatchedUiState(
    val currentCourseId: String = "",
    val videos: Flow<PagingData<YouTubeVideo>> = emptyFlow(),
    val playlistsData: PlaylistsData? = null,
    val channelData: ChannelData? = null,
    val isLoading: Boolean = true,
    val channelPictureUrl: String = "",
    val channelGivenName: String = "",
    val channelFamilyName: String = "",
    val channelEmail: String = ""
)

fun UserCourse.toRecentlyWatchedUiState(
    isLoading: Boolean = true,
    channelPictureUrl: String = "",
    channelGivenName: String = "",
    channelFamilyName: String = "",
    channelEmail: String = ""
): RecentlyWatchedUiState = RecentlyWatchedUiState(
    currentCourseId = id,
    isLoading = isLoading,
    channelEmail = channelEmail,
    channelFamilyName = channelFamilyName,
    channelGivenName = channelGivenName,
    channelPictureUrl = channelPictureUrl
)