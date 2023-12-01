package com.example.myinputlog.data.service.impl

import android.util.Log
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.StorageService
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.google.firebase.perf.trace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class DefaultStorageService @Inject constructor(
    private val auth: AccountService,
    private val firestore: FirebaseFirestore
) : StorageService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val userCourses: Flow<List<UserCourse>>
        get() =
            auth.currentUser.flatMapLatest { user ->
                currentUserCourseCollection(user.id).snapshots()
                    .map { snapshot -> snapshot.toObjects() }
            }

    override suspend fun videosByWatchedOnQuery(courseId: String, lastVideoId: String?, limitSize: Long): Query {
        var query = currentUserCourseCollection(auth.currentUserId)
            .document(courseId)
            .collection(YOU_TUBE_VIDEO_COLLECTION)
            .orderBy("watchedOn", Query.Direction.DESCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (lastVideoId != null) {
            val lastVideo = youTubeVideoCollectionForCurrentUserCourse(auth.currentUserId, courseId).document(lastVideoId).get().await()
            query = query.startAfter(lastVideo)
        }
        return query.limit(limitSize)
    }

    private fun currentUserCourseCollection(uid: String): CollectionReference =
        firestore.collection(USER_COLLECTION).document(uid).collection(USER_COURSE_COLLECTION)

    private fun youTubeVideoCollectionForCurrentUserCourse(uid: String, courseId: String): CollectionReference =
        currentUserCourseCollection(uid).document(courseId).collection(YOU_TUBE_VIDEO_COLLECTION)

    override suspend fun getUserCourse(userCourseId: String): UserCourse? =
        currentUserCourseCollection(auth.currentUserId).document(userCourseId).get().await().toObject()

    override suspend fun saveUserCourse(userCourse: UserCourse): Unit =
        trace(USER_COURSE_SAVE_TRACE) {
            currentUserCourseCollection(auth.currentUserId).add(userCourse).await().id
        }

    override suspend fun updateUserCourse(userCourse: UserCourse): Unit =
        trace(USER_COURSE_UPDATE_TRACE) {
            currentUserCourseCollection(auth.currentUserId).document(userCourse.id).set(userCourse).await()
        }

    override suspend fun deleteUserCourse(userCourseId: String) {
        currentUserCourseCollection(auth.currentUserId).document(userCourseId).delete().await()
    }

    override suspend fun getCourseStatistics(userCourseId: String): CourseStatistics {
        val aggregateField = AggregateField.sum("durationInSeconds")
        val collection = youTubeVideoCollectionForCurrentUserCourse(auth.currentUserId, userCourseId)

        val totalVideosTask = collection.count().get(AggregateSource.SERVER).await()
        val totalTimeTask = collection.aggregate(aggregateField).get(AggregateSource.SERVER).await()
        val todayTimeTask = collection
            .whereGreaterThanOrEqualTo("watchedOn", getStartOfTodayTimestamp())
            .whereLessThan("watchedOn", getStartOfTomorrowTimestamp())
            .aggregate(aggregateField)
            .get(AggregateSource.SERVER)
            .await()

        val totalVideos = totalVideosTask.count
        val totalTime = totalTimeTask.getLong(aggregateField) ?: 0L
        val todayTime = todayTimeTask.getLong(aggregateField) ?: 0L

        return CourseStatistics(
            timeWatched = totalTime,
            timeWatchedToday = todayTime,
            videoCount = totalVideos
        )
    }

    override suspend fun getMonthlyAggregateData(userCourseId: String, yearMonth: YearMonth): List<Long> {
        val aggregateField = AggregateField.sum("durationInSeconds")
        val collection = youTubeVideoCollectionForCurrentUserCourse(auth.currentUserId, userCourseId)

        val daysInMonth = yearMonth.lengthOfMonth()
        val aggregatedDataList = mutableListOf<Long>()

        for (dayOfMonth in 1..daysInMonth) {
            val startOfDay = Date(yearMonth.atDay(dayOfMonth)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000)
            val endOfDay = Date(yearMonth.atDay(dayOfMonth)
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000)
            try {
                val todayTimeTask = collection
                    .whereGreaterThanOrEqualTo("watchedOn", startOfDay)
                    .whereLessThan("watchedOn", endOfDay)
                    .aggregate(aggregateField)
                    .get(AggregateSource.SERVER)
                    .await()
                val todayTime = todayTimeTask.getLong(aggregateField) ?: 0L
                aggregatedDataList.add(todayTime)
            } catch (e: FirebaseFirestoreException) {
                Log.d(TAG, "Error fetching data for day $dayOfMonth: ${e.message}")
                throw e
            }
        }
        return aggregatedDataList
    }

    override suspend fun getYouTubeVideo(userCourseId: String, youTubeVideoId: String): YouTubeVideo? =
        youTubeVideoCollectionForCurrentUserCourse(auth.currentUserId, userCourseId).document(youTubeVideoId).get().await().toObject()

    override suspend fun saveYouTubeVideo(userCourseId: String, youTubeVideo: YouTubeVideo): Unit =
        trace(YOU_TUBE_VIDEO_SAVE_TRACE) {
            youTubeVideoCollectionForCurrentUserCourse(auth.currentUserId, userCourseId).add(youTubeVideo).await().id
        }

    override suspend fun updateYouTubeVideo(userCourseId: String, youTubeVideo: YouTubeVideo): Unit =
        trace(YOU_TUBE_VIDEO_UPDATE_TRACE) {
            youTubeVideoCollectionForCurrentUserCourse(auth.currentUserId, userCourseId).document(youTubeVideo.id).set(youTubeVideo).await()
        }

    override suspend fun deleteYouTubeVideo(userCourseId: String, youTubeVideoId: String) {
        youTubeVideoCollectionForCurrentUserCourse(auth.currentUserId, userCourseId).document(youTubeVideoId).delete().await()
    }

    private fun getStartOfTodayTimestamp(): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Date(calendar.timeInMillis)
    }

    private fun getStartOfTomorrowTimestamp(): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Date(calendar.timeInMillis)
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