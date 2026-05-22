package com.example.myinputlog.data.utils

import android.util.Log
import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
inline fun <reified T : Any> Flow<String>.createReactivePagingFlow(
    courseId: String,
    pagingConfig: PagingConfig,
    crossinline changeSignal: (uid: String) -> Flow<Unit>,
    crossinline factoryProvider: (uid: String) -> PagingSource<DocumentSnapshot, T>
): Flow<PagingData<T>> {
    val tag = T::class.java.simpleName

    return this.flatMapLatest { uid ->
        if (uid.isBlank() || courseId.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            val invalidatingFactory = InvalidatingPagingSourceFactory {
                factoryProvider(uid)
            }

            val pagerFlow = Pager(
                config = pagingConfig, pagingSourceFactory = invalidatingFactory
            ).flow

            channelFlow {
                launch {
                    changeSignal(uid).drop(1).collectLatest {
                        Log.d("PagingHelper", "Invalidation signal received (${tag})")
                        invalidatingFactory.invalidate()
                    }
                }

                pagerFlow.collectLatest { pagingData ->
                    send(pagingData)
                }
            }
        }
    }
}