package com.example.myinputlog.ui.screens.playlists

import androidx.paging.PagingData
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.remote.PlaylistsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class PlaylistsUiState(
    val currentCourseId: String = "",
    val videos: Flow<PagingData<YouTubeVideo>> = emptyFlow(),
    val playlistsData: PlaylistsData? = null,
    val currentPlaylistId: String = PlaylistsViewModel.LIKED_PLAYLIST_ID,
    val isLoading: Boolean = true,
    val channelPictureUrl: String = "",
    val channelGivenName: String = "",
    val channelFamilyName: String = "",
    val channelEmail: String = "",
    val hideEmail: Boolean = false
)

fun UserCourse.toPlaylistsUiState(
    isLoading: Boolean = true,
    channelPictureUrl: String = "",
    channelGivenName: String = "",
    channelFamilyName: String = "",
    channelEmail: String = "",
    hideEmail: Boolean = false
): PlaylistsUiState = PlaylistsUiState(
    currentCourseId = id,
    isLoading = isLoading,
    channelEmail = channelEmail,
    channelFamilyName = channelFamilyName,
    channelGivenName = channelGivenName,
    channelPictureUrl = channelPictureUrl,
    hideEmail = hideEmail
)