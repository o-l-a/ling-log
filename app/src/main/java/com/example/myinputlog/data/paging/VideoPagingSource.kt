package com.example.myinputlog.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VideoPagingSource @Inject constructor(
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService
) : PagingSource<QuerySnapshot, YouTubeVideo>() {
    companion object {
        private const val TAG = "VideoPagingSource"
    }

    override fun getRefreshKey(state: PagingState<QuerySnapshot, YouTubeVideo>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, YouTubeVideo> {
        return try {
            val courseId = preferenceStorageService.currentCourseId.firstOrNull() ?: ""
            val currentPage = params.key ?: storageService
                .videosByWatchedOnQuery(courseId, null, params.loadSize.toLong())
                .get()
                .await()
            val lastVisibleVideo = currentPage.documents.lastOrNull()
            val nextPage = storageService
                .videosByWatchedOnQuery(courseId, lastVisibleVideo, params.loadSize.toLong())
                .get()
                .await()
            LoadResult.Page(
                data = currentPage.toObjects(YouTubeVideo::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
            LoadResult.Error(e)
        }
    }
}