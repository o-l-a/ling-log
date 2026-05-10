package com.example.myinputlog.data.service.impl

import android.util.Log
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.StorageService
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.perf.trace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class DefaultStorageService @Inject constructor(
    private val firestore: FirebaseFirestore
) : StorageService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserCourses(userId: String): Flow<List<UserCourse>> =
        currentUserCourseCollection(userId).snapshots()
            .map { snapshot -> snapshot.toObjects(UserCourse::class.java) }

    override suspend fun videosByWatchedOnQuery(
        userId: String, courseId: String, lastVideo: DocumentSnapshot?, limitSize: Long
    ): Query {
        var query = currentUserCourseCollection(userId).document(courseId)
            .collection(YOU_TUBE_VIDEO_COLLECTION).orderBy("watchedOn", Query.Direction.DESCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (lastVideo != null) {
            query = query.startAfter(lastVideo)
        }
        return query.limit(limitSize)
    }

    private fun currentUserCourseCollection(uid: String): CollectionReference =
        firestore.collection(USER_COLLECTION).document(uid).collection(USER_COURSE_COLLECTION)

    private fun youTubeVideoCollectionForCurrentUserCourse(
        uid: String, courseId: String
    ): CollectionReference =
        currentUserCourseCollection(uid).document(courseId).collection(YOU_TUBE_VIDEO_COLLECTION)

    override suspend fun getUserCourse(currentUserId: String, userCourseId: String): UserCourse? =
        currentUserCourseCollection(currentUserId).document(userCourseId).get().await().toObject()

    override suspend fun saveUserCourse(currentUserId: String, userCourse: UserCourse): String =
        trace(USER_COURSE_SAVE_TRACE) {
            currentUserCourseCollection(currentUserId).add(userCourse).await().id
        }

    override suspend fun updateUserCourse(currentUserId: String, userCourse: UserCourse): Unit =
        trace(USER_COURSE_UPDATE_TRACE) {
            currentUserCourseCollection(currentUserId).document(userCourse.id).set(userCourse)
                .await()
        }

    override suspend fun deleteUserCourse(currentUserId: String, userCourseId: String) {
        currentUserCourseCollection(currentUserId).document(userCourseId).delete().await()
    }

    override suspend fun getCourseStatistics(
        currentUserId: String, userCourseId: String
    ): CourseStatistics = coroutineScope {
        val aggregateField = AggregateField.sum("durationInSeconds")
        val collection = youTubeVideoCollectionForCurrentUserCourse(currentUserId, userCourseId)

        val totalVideosTask = async { collection.count().get(AggregateSource.SERVER).await() }
        val totalTimeTask =
            async { collection.aggregate(aggregateField).get(AggregateSource.SERVER).await() }
        val todayTimeTask = async {
            collection.whereGreaterThanOrEqualTo("watchedOn", getStartOfTodayTimestamp())
                .whereLessThan("watchedOn", getStartOfTomorrowTimestamp()).aggregate(aggregateField)
                .get(AggregateSource.SERVER).await()
        }

        val totalVideos = totalVideosTask.await().count
        val totalTime = totalTimeTask.await().getLong(aggregateField) ?: 0L
        val todayTime = todayTimeTask.await().getLong(aggregateField) ?: 0L

        CourseStatistics(
            timeWatched = totalTime, timeWatchedToday = todayTime, videoCount = totalVideos
        )
    }

    override suspend fun getMonthlyAggregateData(
        currentUserId: String, userCourseId: String, yearMonth: YearMonth
    ): List<Long> = coroutineScope {
        val aggregateField = AggregateField.sum("durationInSeconds")
        val collection = youTubeVideoCollectionForCurrentUserCourse(currentUserId, userCourseId)

        val daysInMonth = yearMonth.lengthOfMonth()

        val tasks = (1..daysInMonth).map { dayOfMonth ->
            async {
                val startOfDay = Date(
                    yearMonth.atDay(dayOfMonth).atStartOfDay(ZoneId.systemDefault())
                        .toEpochSecond() * 1000
                )
                val endOfDay = Date(
                    yearMonth.atDay(dayOfMonth).plusDays(1).atStartOfDay(ZoneId.systemDefault())
                        .toEpochSecond() * 1000
                )
                try {
                    val todayTimeTask =
                        collection.whereGreaterThanOrEqualTo("watchedOn", startOfDay)
                            .whereLessThan("watchedOn", endOfDay).aggregate(aggregateField)
                            .get(AggregateSource.SERVER).await()
                    todayTimeTask.getLong(aggregateField) ?: 0L
                } catch (e: FirebaseFirestoreException) {
                    Log.d(TAG, "Error fetching data for day $dayOfMonth: ${e.message}")
                    throw e
                }
            }
        }
        tasks.awaitAll()
    }

    override suspend fun getYouTubeVideo(
        currentUserId: String, userCourseId: String, youTubeVideoId: String
    ): YouTubeVideo? =
        youTubeVideoCollectionForCurrentUserCourse(currentUserId, userCourseId).document(
            youTubeVideoId
        ).get().await().toObject()

    override suspend fun saveYouTubeVideo(
        currentUserId: String, userCourseId: String, youTubeVideo: YouTubeVideo
    ): Unit = trace(YOU_TUBE_VIDEO_SAVE_TRACE) {
        youTubeVideoCollectionForCurrentUserCourse(currentUserId, userCourseId).add(
            youTubeVideo
        ).await().id
    }

    override suspend fun updateYouTubeVideo(
        currentUserId: String, userCourseId: String, youTubeVideo: YouTubeVideo
    ): Unit = trace(YOU_TUBE_VIDEO_UPDATE_TRACE) {
        youTubeVideoCollectionForCurrentUserCourse(currentUserId, userCourseId).document(
            youTubeVideo.id
        ).set(youTubeVideo).await()
    }

    override suspend fun deleteYouTubeVideo(
        currentUserId: String, userCourseId: String, youTubeVideoId: String
    ) {
        youTubeVideoCollectionForCurrentUserCourse(currentUserId, userCourseId).document(
            youTubeVideoId
        ).delete().await()
    }

    private fun getStartOfTodayTimestamp(): Date {
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Date.from(startOfDay)
    }

    private fun getStartOfTomorrowTimestamp(): Date {
        val startOfTomorrow =
            LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Date.from(startOfTomorrow)
    }

    companion object {
        private const val TAG = "VideoStorageService"
        const val USER_COLLECTION = "users"
        const val USER_COURSE_COLLECTION = "userCourses"
        private const val USER_COURSE_SAVE_TRACE = "saveUserCourse"
        private const val USER_COURSE_UPDATE_TRACE = "updateUserCourse"
        const val YOU_TUBE_VIDEO_COLLECTION = "youTubeVideos"
        private const val YOU_TUBE_VIDEO_SAVE_TRACE = "saveYouTubeVideo"
        private const val YOU_TUBE_VIDEO_UPDATE_TRACE = "updateYouTubeVideo"
    }
}