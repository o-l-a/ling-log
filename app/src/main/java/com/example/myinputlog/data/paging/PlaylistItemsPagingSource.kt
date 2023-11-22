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
                if (response.isSuccessful && response.body() != null) {
                    val currentPage = response.body()!!.items.map { playlistItem ->
                        videoDataRepository.getVideoData(playlistItem.contentDetails.videoId).let {
                            if (it.isSuccessful && it.body() != null) {
                                it.body()!!.toYouTubeVideo()!!
                            } else {
                                YouTubeVideo()
                            }
                        }
                    }
                    Log.d(TAG, currentPage.size.toString())
                    LoadResult.Page(
                        data = currentPage,
                        prevKey = null,
                        nextKey = response.body()!!.nextPageToken
                    )
                }
                LoadResult.Error(NetworkErrorException(response.message().toString()))
            }
        } catch (e: InvocationTargetException) {
            e.targetException.message?.let { Log.d(TAG, it) }
            LoadResult.Error(e)
        }
    }
}