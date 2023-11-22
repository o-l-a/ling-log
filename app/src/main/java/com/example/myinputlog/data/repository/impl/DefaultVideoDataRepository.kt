package com.example.myinputlog.data.repository.impl

import com.example.myinputlog.data.remote.ChannelData
import com.example.myinputlog.data.remote.PlaylistItemsData
import com.example.myinputlog.data.remote.PlaylistsData
import com.example.myinputlog.data.remote.VideoData
import com.example.myinputlog.data.repository.VideoDataRepository
import com.example.myinputlog.data.service.ApiService
import retrofit2.Response

class DefaultVideoDataRepository(private val apiService: ApiService) : VideoDataRepository {
    override suspend fun getVideoData(videoId: String): Response<VideoData> =
        apiService.getVideoData(videoId)

    override suspend fun getChannelData(token: String): Response<ChannelData> =
        apiService.getChannelData(token)

    override suspend fun getPlaylistsData(token: String): Response<PlaylistsData> =
        apiService.getPlaylistsData(token)

    override suspend fun getPlaylistItemsData(
        token: String,
        playlistId: String,
        maxResults: Int,
        pageToken: String?
    ): Response<PlaylistItemsData> =
        apiService.getPlaylistItemsIds(token, playlistId, maxResults, pageToken)
}