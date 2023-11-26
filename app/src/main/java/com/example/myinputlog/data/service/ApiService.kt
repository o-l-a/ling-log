package com.example.myinputlog.data.service

import com.example.myinputlog.BuildConfig
import com.example.myinputlog.data.remote.ChannelData
import com.example.myinputlog.data.remote.PlaylistItemsData
import com.example.myinputlog.data.remote.PlaylistsData
import com.example.myinputlog.data.remote.VideoData
import com.example.myinputlog.ui.screens.utils.PAGE_SIZE
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

    @GET("playlistItems")
    suspend fun getPlaylistItemsIds(
        @Header("Authorization") token: String,
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = PAGE_SIZE,
        @Query("pageToken") pageToken: String? = null,
        @Query("part") part: String = "contentDetails",
        @Query("mine") mine: String = "true",
        @Query("fields") fields: String = "nextPageToken,items(contentDetails(videoId))"
    ): Response<PlaylistItemsData>
}