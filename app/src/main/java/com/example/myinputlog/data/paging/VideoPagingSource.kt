package com.example.myinputlog.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.lang.reflect.InvocationTargetException
import javax.inject.Inject

class VideoPagingSource @Inject constructor(
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService
) : PagingSource<DocumentSnapshot, YouTubeVideo>() {
    companion object {
        private const val TAG = "VideoPagingSource"
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, YouTubeVideo>): DocumentSnapshot? =
        null

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, YouTubeVideo> {
        return try {
            val courseId = preferenceStorageService.currentCourseId.firstOrNull() ?: ""
            val querySnapshot = storageService.videosByWatchedOnQuery(
                    courseId,
                    params.key,
                    params.loadSize.toLong()
                ).get().await()
            val currentPage = querySnapshot.toObjects(YouTubeVideo::class.java)
            val nextKey = querySnapshot.documents.lastOrNull()
            LoadResult.Page(
                data = currentPage, prevKey = null, nextKey = nextKey
            )
        } catch (e: InvocationTargetException) {
            e.targetException.message?.let { Log.d(TAG, it) }
            LoadResult.Error(e)
        }
    }
}