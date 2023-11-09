package com.example.myinputlog.data.repository

import com.example.myinputlog.data.model.VideoData
import retrofit2.Response

interface VideoDataRepository {
    suspend fun getVideoData(videoId: String): Response<VideoData>
}