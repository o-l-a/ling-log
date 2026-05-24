package com.example.myinputlog.data.repository

import com.example.myinputlog.data.remote.ChannelData
import com.example.myinputlog.data.remote.VideoData

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class NetworkError(val message: String, val throwable: Throwable? = null) : DataResult<Nothing>
    data class ApiError(val message: String, val throwable: Throwable? = null) : DataResult<Nothing>
}

interface ApiDataRepository {
    suspend fun getVideoData(videoId: String): DataResult<VideoData>

    suspend fun getChannelData(channelId: String): DataResult<ChannelData>
}