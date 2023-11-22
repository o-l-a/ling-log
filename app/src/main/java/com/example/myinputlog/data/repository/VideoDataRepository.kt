package com.example.myinputlog.data.repository

import com.example.myinputlog.data.remote.ChannelData
import com.example.myinputlog.data.remote.PlaylistItemsData
import com.example.myinputlog.data.remote.PlaylistsData
import com.example.myinputlog.data.remote.VideoData
import retrofit2.Response

interface VideoDataRepository {
    suspend fun getVideoData(videoId: String): Response<VideoData>

    suspend fun getChannelData(token: String): Response<ChannelData>

    suspend fun getPlaylistsData(token: String): Response<PlaylistsData>

    suspend fun getPlaylistItemsData(
        token: String,
        playlistId: String,
        maxResults: Int,
        pageToken: String?
    ): Response<PlaylistItemsData>
}