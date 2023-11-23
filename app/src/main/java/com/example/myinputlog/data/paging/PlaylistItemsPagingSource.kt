package com.example.myinputlog.data.paging

import android.accounts.NetworkErrorException
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.remote.toYouTubeVideo
import com.example.myinputlog.data.repository.impl.DefaultVideoDataRepository
import java.lang.reflect.InvocationTargetException

class PlaylistItemsPagingSource(
    private val token: String,
    private val playlistId: String,
    private val videoDataRepository: DefaultVideoDataRepository
) : PagingSource<String, YouTubeVideo>() {
    companion object {
        private const val TAG = "PlaylistItemsIdsPagingSource"
    }

    override fun getRefreshKey(state: PagingState<String, YouTubeVideo>): String = ""

    override suspend fun load(params: LoadParams<String>): LoadResult<String, YouTubeVideo> {
        return try {
            val currentPageToken = params.key
            videoDataRepository.getPlaylistItemsData(
                token = token,
                playlistId = playlistId,
                maxResults = params.loadSize,
                pageToken = currentPageToken
            ).let { response ->
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val currentPage = responseBody.items.mapNotNull { playlistItem ->
                        videoDataRepository.getVideoData(playlistItem.contentDetails.videoId).let {
                            val videoResponseBody = it.body()
                            if (it.isSuccessful && videoResponseBody != null) {
                                videoResponseBody.toYouTubeVideo()
                            } else {
                                null
                            }
                        }
                    }
                    val nextKey = responseBody.nextPageToken
                    Log.d(TAG, "Next page: $nextKey")
                    Log.d(TAG, "Item count: ${currentPage.size}")
                    LoadResult.Page(
                        data = currentPage,
                        prevKey = null,
                        nextKey = nextKey
                    )
                } else {
                    Log.d(TAG, "network error")
                    LoadResult.Error(NetworkErrorException(response.message().toString()))
                }
            }
        } catch (e: InvocationTargetException) {
            e.targetException.message?.let { Log.d(TAG, it) }
            LoadResult.Error(e)
        }
    }
}