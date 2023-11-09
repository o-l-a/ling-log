package com.example.myinputlog.data.repository.impl

import com.example.myinputlog.data.model.VideoData
import com.example.myinputlog.data.repository.VideoDataRepository
import com.example.myinputlog.data.service.ApiService
import retrofit2.Response

class DefaultVideoDataRepository(private val apiService: ApiService) : VideoDataRepository {
    override suspend fun getVideoData(videoId: String): Response<VideoData> =
        apiService.getVideoData(videoId)
}