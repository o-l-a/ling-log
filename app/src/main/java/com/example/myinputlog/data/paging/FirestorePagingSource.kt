package com.example.myinputlog.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

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
        return try {
            val query = queryProvider(
                params.key, params.loadSize.toLong()
            )
            val querySnapshot = try {
                Log.d(TAG, "Read the collection from cache (${modelClass.simpleName})")
                query.get(Source.CACHE).await()
            } catch (e: FirebaseFirestoreException) {
                Log.d(TAG, "Read the collection from server (${modelClass.simpleName})", e)
                query.get(Source.SERVER).await()
            }
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