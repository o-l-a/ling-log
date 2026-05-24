package com.example.myinputlog.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestorePagingSource<T : Any>(
    private val modelClass: Class<T>,
    private val queryProvider: suspend (key: DocumentSnapshot?, loadSize: Long) -> Query
) : PagingSource<DocumentSnapshot, T>() {

    companion object {
        private const val TAG = "FirestorePagingSource"
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, T>): DocumentSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, T> {
        return withContext(Dispatchers.IO) {
            try {
                val query = queryProvider(
                    params.key, params.loadSize.toLong()
                )
                val querySnapshot = query.get().await()
                val querySource = if (querySnapshot.metadata.isFromCache) "CACHE" else "SERVER"
                val readCount = querySnapshot.size()
                Log.d(TAG, "Read $readCount ${modelClass.simpleName}(s) from $querySource")
                val currentPage = querySnapshot.toObjects(modelClass)
                val nextKey =
                    if (currentPage.size < params.loadSize) null else querySnapshot.documents.lastOrNull()
                LoadResult.Page(
                    data = currentPage, prevKey = null, nextKey = nextKey
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load page for (${modelClass.simpleName})", e)
                LoadResult.Error(e)
            }
        }
    }
}