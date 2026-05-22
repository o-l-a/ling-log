package com.example.myinputlog.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.StorageService
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

class VideoPagingSource @AssistedInject constructor(
    private val storageService: StorageService,
    @Assisted("userId") private val userId: String,
    @Assisted("courseId") private val courseId: String
) : PagingSource<DocumentSnapshot, YouTubeVideo>() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: String, @Assisted("courseId") courseId: String
        ): VideoPagingSource
    }

    companion object {
        private const val TAG = "VideoPagingSource"
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, YouTubeVideo>): DocumentSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, YouTubeVideo> {
        return try {
            val query = storageService.videosByWatchedOnQuery(
                userId, courseId, params.key, params.loadSize.toLong()
            )
            val querySnapshot = try {
                Log.d(TAG, "Read the collection from cache (video)")
                query.get(Source.CACHE).await()
            } catch (e: FirebaseFirestoreException) {
                Log.d(TAG, "Read the collection from server (video)", e)
                query.get(Source.SERVER).await()
            }
            val currentPage = querySnapshot.toObjects(YouTubeVideo::class.java)
            val nextKey =
                if (currentPage.size < params.loadSize) null else querySnapshot.documents.lastOrNull()
            LoadResult.Page(
                data = currentPage, prevKey = null, nextKey = nextKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load page", e)
            LoadResult.Error(e)
        }
    }
}