package com.example.myinputlog.data.repository.impl

import com.example.myinputlog.data.remote.ChannelItem
import com.example.myinputlog.data.remote.VideoItem
import com.example.myinputlog.data.repository.ApiDataRepository
import com.example.myinputlog.data.repository.DataResult
import com.example.myinputlog.data.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultApiDataRepository(private val apiService: ApiService) : ApiDataRepository {
    override suspend fun getVideoData(videoId: String): DataResult<VideoItem> = withContext(
        Dispatchers.IO
    ) {
        try {
            val response = apiService.getVideoData(videoId)
            val body = response.body()
            if (response.isSuccessful && body != null && body.items.count() > 0) {
                DataResult.Success(body.items[0])
            } else {
                DataResult.ApiError("API error")
            }
        } catch (e: Exception) {
            DataResult.NetworkError("Network error", e)
        }
    }

    override suspend fun getChannelData(channelId: String): DataResult<ChannelItem> = withContext(
        Dispatchers.IO
    ) {
        try {
            val response = apiService.getChannelData(channelId)
            val body = response.body()
            if (response.isSuccessful && body != null && body.items.count() > 0) {
                DataResult.Success(body.items[0])
            } else {
                DataResult.ApiError("API error")
            }
        } catch (e: Exception) {
            DataResult.NetworkError("Network error", e)
        }
    }
}