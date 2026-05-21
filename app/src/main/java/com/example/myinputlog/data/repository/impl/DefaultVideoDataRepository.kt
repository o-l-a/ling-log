package com.example.myinputlog.data.repository.impl

import com.example.myinputlog.data.remote.ChannelData
import com.example.myinputlog.data.remote.VideoData
import com.example.myinputlog.data.repository.DataResult
import com.example.myinputlog.data.repository.VideoDataRepository
import com.example.myinputlog.data.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultVideoDataRepository(private val apiService: ApiService) : VideoDataRepository {
    override suspend fun getVideoData(videoId: String): DataResult<VideoData> = withContext(
        Dispatchers.IO
    ) {
        try {
            val response = apiService.getVideoData(videoId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                DataResult.Success(body)
            } else {
                DataResult.ApiError("API error")
            }
        } catch (e: Exception) {
            DataResult.NetworkError("Network error", e)
        }
    }

    override suspend fun getChannelData(channelId: String): DataResult<ChannelData> = withContext(
        Dispatchers.IO
    ) {
        try {
            val response = apiService.getChannelData(channelId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                DataResult.Success(body)
            } else {
                DataResult.ApiError("API error")
            }
        } catch (e: Exception) {
            DataResult.NetworkError("Network error", e)
        }
    }
}