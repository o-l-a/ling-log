package com.example.myinputlog.data.repository

import com.example.myinputlog.data.remote.ChannelItem
import com.example.myinputlog.data.remote.VideoItem

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class NetworkError(val message: String, val throwable: Throwable? = null) :
        DataResult<Nothing>

    data class ApiError(val message: String, val throwable: Throwable? = null) : DataResult<Nothing>
}

interface ApiDataRepository {
    suspend fun getVideoData(videoId: String): DataResult<VideoItem>

    suspend fun getChannelData(channelId: String): DataResult<ChannelItem>
}