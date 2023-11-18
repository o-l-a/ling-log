package com.example.myinputlog.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.lang.reflect.InvocationTargetException
import javax.inject.Inject

class VideoPagingSource @Inject constructor(
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService
) : PagingSource<String, YouTubeVideo>() {
    companion object {
        private const val TAG = "VideoPagingSource"
    }

    override fun getRefreshKey(state: PagingState<String, YouTubeVideo>): String = ""

    override suspend fun load(params: LoadParams<String>): LoadResult<String, YouTubeVideo> {
        return try {
            val courseId = preferenceStorageService.currentCourseId.firstOrNull() ?: ""
            val lastVisibleVideoId = params.key
            val currentPage = storageService
                .videosByWatchedOnQuery(courseId, lastVisibleVideoId, params.loadSize.toLong())
                .get()
                .await()
                .toObjects(YouTubeVideo::class.java)
            val nextKey = currentPage.lastOrNull()?.id
            LoadResult.Page(
                data = currentPage,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: InvocationTargetException) {
            e.targetException.message?.let { Log.d(TAG, it) }
            LoadResult.Error(e)
        }
    }
}