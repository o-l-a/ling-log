package com.example.myinputlog.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.service.StorageService
import com.google.firebase.firestore.DocumentSnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

class ChannelPagingSource @AssistedInject constructor(
    private val storageService: StorageService,
    @Assisted("userId") private val userId: String,
    @Assisted("courseId") private val courseId: String
) : PagingSource<DocumentSnapshot, YouTubeChannel>() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: String, @Assisted("courseId") courseId: String
        ): VideoPagingSource
    }

    companion object {
        private const val TAG = "ChannelPagingSource"
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, YouTubeChannel>): DocumentSnapshot? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey ?: state.closestPageToPosition(
                anchorPosition
            )?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, YouTubeChannel> {
        return try {
            val querySnapshot = storageService.videosByWatchedOnQuery(
                userId, courseId, params.key, params.loadSize.toLong()
            ).get().await()
            val currentPage = querySnapshot.toObjects(YouTubeChannel::class.java)
            val nextKey = querySnapshot.documents.lastOrNull()
            LoadResult.Page(
                data = currentPage, prevKey = null, nextKey = nextKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load page", e)
            LoadResult.Error(e)
        }
    }
}