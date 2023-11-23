package com.example.myinputlog.ui.screens.playlists

import androidx.paging.PagingData
import com.example.myinputlog.data.remote.ChannelData
import com.example.myinputlog.data.remote.PlaylistsData
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class PlaylistsUiState(
    val currentCourseId: String = "",
    val videos: Flow<PagingData<YouTubeVideo>> = emptyFlow(),
    val playlistsData: PlaylistsData? = null,
    val channelData: ChannelData? = null,
    val currentPlaylistId: String = PlaylistsViewModel.LIKED_PLAYLIST_ID,
    val isLoading: Boolean = true,
    val channelPictureUrl: String = "",
    val channelGivenName: String = "",
    val channelFamilyName: String = "",
    val channelEmail: String = ""
)

fun UserCourse.toPlaylistsUiState(
    isLoading: Boolean = true,
    channelPictureUrl: String = "",
    channelGivenName: String = "",
    channelFamilyName: String = "",
    channelEmail: String = ""
): PlaylistsUiState = PlaylistsUiState(
    currentCourseId = id,
    isLoading = isLoading,
    channelEmail = channelEmail,
    channelFamilyName = channelFamilyName,
    channelGivenName = channelGivenName,
    channelPictureUrl = channelPictureUrl
)