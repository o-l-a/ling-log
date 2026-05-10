package com.example.myinputlog.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.google.firebase.firestore.DocumentSnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

class VideoPagingSource @AssistedInject constructor(
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService,
    @Assisted("userId") private val userId: String,
    @Assisted("courseId") private val courseId: String
) : PagingSource<DocumentSnapshot, YouTubeVideo>() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: String,
            @Assisted("courseId") courseId: String
        ): VideoPagingSource
    }

    companion object {
        private const val TAG = "VideoPagingSource"
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, YouTubeVideo>): DocumentSnapshot? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey ?: state.closestPageToPosition(
                anchorPosition
            )?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, YouTubeVideo> {
        return try {
            val courseId = preferenceStorageService.currentCourseId.firstOrNull() ?: ""
            val querySnapshot = storageService.videosByWatchedOnQuery(
                userId, courseId, params.key, params.loadSize.toLong()
            ).get().await()
            val currentPage = querySnapshot.toObjects(YouTubeVideo::class.java)
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