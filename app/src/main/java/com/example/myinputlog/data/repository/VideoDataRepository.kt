package com.example.myinputlog.data.repository

import com.example.myinputlog.data.model.ChannelData
import com.example.myinputlog.data.model.PlaylistsData
import com.example.myinputlog.data.model.VideoData
import retrofit2.Response

interface VideoDataRepository {
    suspend fun getVideoData(videoId: String): Response<VideoData>

    suspend fun getChannelData(token: String): Response<ChannelData>

    suspend fun getPlaylistsData(token: String): Response<PlaylistsData>
}