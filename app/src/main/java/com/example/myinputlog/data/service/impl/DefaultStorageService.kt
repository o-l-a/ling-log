package com.example.myinputlog.data.service.impl

import android.util.Log
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.StorageService
import com.example.myinputlog.data.utils.DateUtils.getStartOfTodayTimestamp
import com.example.myinputlog.data.utils.DateUtils.getStartOfTomorrowTimestamp
import com.example.myinputlog.data.utils.DateUtils.toDayKey
import com.example.myinputlog.data.utils.DateUtils.toMonthKey
import com.example.myinputlog.data.utils.toFirestoreMap
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.perf.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class DefaultStorageService @Inject constructor(
    private val firestore: FirebaseFirestore
) : StorageService {

    companion object {
        private const val TAG = "VideoStorageService"

        // collections
        const val USER_COLL = "users"
        const val USER_COURSE_COLL = "userCourses"
        const val YT_VIDEO_COLL = "youTubeVideos"
        const val YT_CHANNEL_COLL = "youTubeChannels"
        private const val USER_LABEL_COLL = "userLabels"
        private const val USER_MONTHLY_STATS_COLL = "userMonthlyStats"
        private const val LABEL_CHANNEL_STATS_COLL = "channelStats"

        // field names
        private const val KEY_DURATION = "totalTimeInSeconds"
        private const val KEY_COUNT = "totalVideoCount"
        private const val KEY_DAYS = "days"
        private const val KEY_ID = "id"
        private const val DAY_CHANNEL_MAP = "channelBreakdown"
        private const val DAY_LABEL_MAP = "labelBreakdown"

        // traces
        private const val USER_COURSE_SAVE_TRACE = "saveUserCourse"
        private const val USER_COURSE_UPDATE_TRACE = "updateUserCourse"
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserCourses(userId: String): Flow<List<UserCourse>> =
        currentUserCourseCollection(userId).snapshots()
            .map { snapshot -> snapshot.toObjects(UserCourse::class.java) }.flowOn(Dispatchers.IO)

    override suspend fun videosByWatchedOnQuery(
        userId: String, courseId: String, lastVideo: DocumentSnapshot?, limitSize: Long
    ): Query {
        var query = currentUserCourseCollection(userId).document(courseId).collection(YT_VIDEO_COLL)
            .orderBy("watchedOn", Query.Direction.DESCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (lastVideo != null) {
            query = query.startAfter(lastVideo)
        }
        return query.limit(limitSize)
    }

    private fun currentUserCourseCollection(uid: String): CollectionReference =
        firestore.collection(USER_COLL).document(uid).collection(USER_COURSE_COLL)

    private fun youTubeVideoCollectionForCurrentUserCourse(
        uid: String, courseId: String
    ): CollectionReference =
        currentUserCourseCollection(uid).document(courseId).collection(YT_VIDEO_COLL)

    private fun youTubeChannelCollectionForCurrentUserCourse(
        uid: String, courseId: String
    ): CollectionReference =
        currentUserCourseCollection(uid).document(courseId).collection(YT_CHANNEL_COLL)

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


    override suspend fun deleteUserCourse(currentUserId: String, userCourseId: String): Unit {
        currentUserCourseCollection(currentUserId).document(userCourseId).delete().await()
    }


    override suspend fun getCourseStatistics(
        currentUserId: String, userCourseId: String
    ): CourseStatistics = withContext(Dispatchers.IO) {
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
    ): List<Long> = withContext(Dispatchers.IO) {
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

    override suspend fun getYouTubeChannel(
        currentUserId: String, userCourseId: String, youTubeChannelId: String
    ): YouTubeChannel? =
        youTubeChannelCollectionForCurrentUserCourse(currentUserId, userCourseId).document(
            youTubeChannelId
        ).get().await().toObject()

    override suspend fun saveYouTubeVideo(
        userId: String,
        courseId: String,
        newVideo: YouTubeVideo,
        oldVideo: YouTubeVideo?,
        channelMetadata: YouTubeChannel?,
        channelExistsOnServer: Boolean
    ) {
        oldVideo?.let { old ->
            require(old.channelId == newVideo.channelId) { "Cannot change channel on existing video." }
            require(old.videoUrl == newVideo.videoUrl) { "Cannot change URL on existing video." }
        }

        val batch = firestore.batch()
        val acc = FirestoreAccumulator(batch)
        val courseRef = currentUserCourseCollection(userId).document(courseId)

        // stats down for the original copy
        oldVideo?.let { applyStats(acc, courseRef, oldVideo, multiplier = -1L) }
        // stats up for the new copy
        applyStats(acc, courseRef, newVideo, multiplier = 1L)

        if (!channelExistsOnServer && channelMetadata != null) {
            // only save if this is a new channel
            val channelRef = courseRef.collection(YT_CHANNEL_COLL).document(newVideo.channelId)
            acc.setChannelMetadata(channelRef, channelMetadata)
        }

        acc.applyToBatch()

        val videoRef = if (oldVideo != null && newVideo.id.isNotBlank()) {
            courseRef.collection(YT_VIDEO_COLL).document(newVideo.id)
        } else {
            courseRef.collection(YT_VIDEO_COLL).document()
        }
        batch.set(videoRef, newVideo.copy(id = videoRef.id))

        batch.commit().await()

    }

    override suspend fun deleteYouTubeVideo(
        userId: String, courseId: String, video: YouTubeVideo
    ) {
        val batch = firestore.batch()
        val acc = FirestoreAccumulator(batch)
        val courseRef = currentUserCourseCollection(userId).document(courseId)

        // stats down
        applyStats(acc, courseRef, video, multiplier = -1L)
        acc.applyToBatch()

        // delete video
        val videoRef = courseRef.collection(YT_VIDEO_COLL).document(video.id)
        batch.delete(videoRef)

        batch.commit().await()
    }

    private fun applyStats(
        acc: FirestoreAccumulator,
        courseRef: DocumentReference,
        video: YouTubeVideo,
        multiplier: Long
    ) {
        val dur = video.durationInSeconds * multiplier
        val count = 1L * multiplier
        val channelId = video.channelId

        // 1. Global & Channel Updates
        val channelRef = courseRef.collection(YT_CHANNEL_COLL).document(channelId)
        acc.increment(courseRef, KEY_DURATION, dur)
        acc.increment(courseRef, KEY_COUNT, count)
        acc.increment(channelRef, KEY_DURATION, dur)
        acc.increment(channelRef, KEY_COUNT, count)

        // 2. Monthly Updates
        val monthKey = video.watchedOn.toMonthKey()
        val dayKey = video.watchedOn.toDayKey()
        val monthRef = courseRef.collection(USER_MONTHLY_STATS_COLL).document(monthKey)

        acc.increment(monthRef, KEY_DURATION, dur)
        acc.increment(monthRef, KEY_COUNT, count)
        acc.increment(monthRef, "$KEY_DAYS.$dayKey.$KEY_DURATION", dur)
        acc.increment(monthRef, "$KEY_DAYS.$dayKey.$KEY_COUNT", count)
        acc.increment(monthRef, "$KEY_DAYS.$dayKey.$DAY_CHANNEL_MAP.$channelId", dur)

        // 3. Label Updates
        video.labelIds.forEach { labelId ->
            val labelRef = courseRef.collection(USER_LABEL_COLL).document(labelId)
            val intersectRef = labelRef.collection(LABEL_CHANNEL_STATS_COLL).document(channelId)

            acc.increment(labelRef, KEY_DURATION, dur)
            acc.increment(labelRef, KEY_COUNT, count)
            acc.increment(intersectRef, KEY_DURATION, dur)
            acc.increment(intersectRef, KEY_COUNT, count)

            acc.increment(monthRef, "$KEY_DAYS.$dayKey.$DAY_LABEL_MAP.$labelId", dur)

            if (multiplier == 1L) { // when updating, this should already be set
                acc.setString(intersectRef, "channelTitle", video.channel)
            }
        }
    }

    private class FirestoreAccumulator(private val batch: WriteBatch) {
        private val increments = mutableMapOf<DocumentReference, MutableMap<String, Long>>()
        private val strings = mutableMapOf<DocumentReference, MutableMap<String, String>>()
        private val documentUpdates = mutableMapOf<DocumentReference, MutableMap<String, Any>>()

        fun increment(ref: DocumentReference, field: String, amount: Long) {
            if (amount == 0L) return
            val docMap = increments.getOrPut(ref) { mutableMapOf() }
            docMap[field] = (docMap[field] ?: 0L) + amount
        }

        fun setString(ref: DocumentReference, field: String, value: String) {
            val docMap = strings.getOrPut(ref) { mutableMapOf() }
            docMap[field] = value
        }

        fun setChannelMetadata(ref: DocumentReference, channel: YouTubeChannel) {
            val updates = documentUpdates.getOrPut(ref) { mutableMapOf() }
            val channelMap = Json.encodeToJsonElement(channel).jsonObject.toFirestoreMap()

            channelMap.remove(KEY_DURATION)
            channelMap.remove(KEY_COUNT)
            channelMap.remove(KEY_ID)

            updates.putAll(channelMap)
        }

        fun applyToBatch() {
            val allRefs = increments.keys + strings.keys + documentUpdates.keys
            for (ref in allRefs) {
                val data = mutableMapOf<String, Any>()
                strings[ref]?.forEach { (key, value) ->
                    data.putPath(key, value)
                }
                increments[ref]?.forEach { (key, value) ->
                    if (value != 0L) data.putPath(key, FieldValue.increment(value))
                }
                documentUpdates[ref]?.forEach { (key, value) ->
                    data.putPath(key, value)
                }
                if (data.isNotEmpty()) {
                    // only update if value actually changed
                    batch.set(ref, data, SetOptions.merge())
                }
            }
        }

        /**
         * Convert fields with dot notation into a map.
         */
        @Suppress("UNCHECKED_CAST")
        private fun MutableMap<String, Any>.putPath(path: String, value: Any) {
            val keys = path.split('.')
            var current = this
            for (i in 0 until keys.size - 1) {
                current =
                    current.getOrPut(keys[i]) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
            }
            current[keys.last()] = value
        }
    }
}