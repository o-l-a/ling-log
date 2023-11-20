package com.example.myinputlog.data.service

import com.example.myinputlog.BuildConfig
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

    @GET("playlistItems")
    suspend fun getPlaylistItemsIds(
        @Query("mine") mine: String = "true",
    ): Response<List<String>>

    @GET("channels")
    suspend fun getMyChannelInfo(
        @Query("part") part: String = "contentDetails",
        @Query("mine") mine: String = "true",
        @Header("Authorization") token: String
    ): Response<String>
}