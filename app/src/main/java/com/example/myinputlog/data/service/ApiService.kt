package com.example.myinputlog.data.service

import com.example.myinputlog.BuildConfig
import com.example.myinputlog.data.model.ChannelData
import com.example.myinputlog.data.model.PlaylistsData
import com.example.myinputlog.data.model.VideoData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

private const val API_KEY: String = BuildConfig.API_KEY

interface ApiService {
    @GET("videos")
    suspend fun getVideoData(
        @Query("id") videoId: String,
        @Query("key") apiKey: String = API_KEY,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("fields") fields: String = "items(id,snippet,contentDetails)"
    ): Response<VideoData>

    @GET("channels")
    suspend fun getChannelData(
        @Header("Authorization") token: String,
        @Query("part") part: String = "contentDetails",
        @Query("mine") mine: String = "true",
        @Query("fields") fields: String = "items(contentDetails(relatedPlaylists(likes)))"
    ): Response<ChannelData>

    @GET("playlists")
    suspend fun getPlaylistsData(
        @Header("Authorization") token: String,
        @Query("part") part: String = "contentDetails,snippet",
        @Query("mine") mine: String = "true",
        @Query("fields") fields: String = "items(id,snippet(title))"
    ): Response<PlaylistsData>
}