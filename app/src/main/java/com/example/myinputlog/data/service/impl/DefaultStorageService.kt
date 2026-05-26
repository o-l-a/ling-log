package com.example.myinputlog.data.service.impl

import android.util.Log
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.data.model.YouTubeChannel
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.StorageService
import com.example.myinputlog.data.utils.DateUtils.toDayKey
import com.example.myinputlog.data.utils.DateUtils.toMonthKey
import com.example.myinputlog.data.utils.toFirestoreMap
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.perf.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
        private const val KEY_WATCHED_ON = "watchedOn"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_LAST_UPDATE = "lastUpdated"
        private const val KEY_TITLE = "title"
        private const val KEY_TOTAL_ACTIVE_DAYS = "totalActiveDays"
        private const val DAY_CHANNEL_MAP = "channelBreakdown"
        private const val DAY_LABEL_MAP = "labelBreakdown"

        // traces
        private const val USER_COURSE_SAVE_TRACE = "saveUserCourse"
        private const val USER_COURSE_UPDATE_TRACE = "updateUserCourse"
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserCourses(userId: String): Flow<List<UserCourse>> =
        currentUserCourseColl(userId).snapshots()
            .map { snapshot -> snapshot.toObjects(UserCourse::class.java) }.flowOn(Dispatchers.IO)

    override suspend fun videosByWatchedOnQuery(
        userId: String, courseId: String, lastVideo: DocumentSnapshot?, limitSize: Long
    ): Query = currentUserCourseColl(userId).document(courseId).collection(YT_VIDEO_COLL)
        .orderBy(KEY_WATCHED_ON, Query.Direction.DESCENDING)
        .orderBy(KEY_TIMESTAMP, Query.Direction.DESCENDING).limit(limitSize).let { query ->
            lastVideo?.let { query.startAfter(it) } ?: query
        }

    override suspend fun channelsByVideoCount(
        userId: String, courseId: String, lastChannel: DocumentSnapshot?, limitSize: Long
    ): Query = currentUserCourseColl(userId).document(courseId).collection(YT_CHANNEL_COLL)
        .orderBy(KEY_COUNT, Query.Direction.DESCENDING)
        .orderBy(KEY_TITLE, Query.Direction.ASCENDING).limit(limitSize).let { query ->
            lastChannel?.let { query.startAfter(it) } ?: query
        }

    override fun getVideosChangeSignal(
        userId: String, courseId: String
    ): Flow<Unit> {
        return currentUserCourseColl(userId).document(courseId).snapshots().map { snapshot ->
            snapshot.getTimestamp(KEY_LAST_UPDATE)?.seconds ?: 0L
        }.distinctUntilChanged().map { }
    }

    private fun currentUserCourseColl(uid: String): CollectionReference =
        firestore.collection(USER_COLL).document(uid).collection(USER_COURSE_COLL)

    private fun youTubeVideoCollForCurrentCourse(
        uid: String, courseId: String
    ): CollectionReference = currentUserCourseColl(uid).document(courseId).collection(YT_VIDEO_COLL)

    private fun youTubeChannelCollForCurrentCourse(
        uid: String, courseId: String
    ): CollectionReference =
        currentUserCourseColl(uid).document(courseId).collection(YT_CHANNEL_COLL)

    private fun monthlyStatsCollForCurrentCourse(
        uid: String, courseId: String
    ): CollectionReference =
        currentUserCourseColl(uid).document(courseId).collection(USER_MONTHLY_STATS_COLL)

    override suspend fun getUserCourse(currentUserId: String, userCourseId: String): UserCourse? =
        currentUserCourseColl(currentUserId).document(userCourseId).get().await().toObject()


    override suspend fun saveUserCourse(currentUserId: String, userCourse: UserCourse): String =
        trace(USER_COURSE_SAVE_TRACE) {
            currentUserCourseColl(currentUserId).add(userCourse).await().id
        }

    override suspend fun updateUserCourse(currentUserId: String, userCourse: UserCourse): Unit =
        trace(USER_COURSE_UPDATE_TRACE) {
            currentUserCourseColl(currentUserId).document(userCourse.id).set(userCourse).await()
        }

    override suspend fun deleteUserCourse(currentUserId: String, userCourseId: String) {
        currentUserCourseColl(currentUserId).document(userCourseId).delete().await()
    }

    override fun getMonthlyStatsFlow(
        currentUserId: String, userCourseId: String, monthId: String
    ): Flow<UserMonthlyStats?> {
        return monthlyStatsCollForCurrentCourse(currentUserId, userCourseId).document(monthId)
            .snapshots().map { snapshot -> snapshot.toObject<UserMonthlyStats>() }
    }

    override suspend fun getYouTubeVideo(
        currentUserId: String, userCourseId: String, youTubeVideoId: String
    ): YouTubeVideo? = youTubeVideoCollForCurrentCourse(currentUserId, userCourseId).document(
        youTubeVideoId
    ).get().await().toObject()

    override suspend fun getYouTubeChannel(
        currentUserId: String, userCourseId: String, youTubeChannelId: String
    ): YouTubeChannel? = youTubeChannelCollForCurrentCourse(currentUserId, userCourseId).document(
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
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Saving video ${newVideo.title}")
            oldVideo?.let { old ->
                require(old.channelId == newVideo.channelId) { "Cannot change channel on existing video." }
                require(old.videoUrl == newVideo.videoUrl) { "Cannot change URL on existing video." }
            }

            if (newVideo == oldVideo) {
                return@withContext
            }

            val courseRef = currentUserCourseColl(userId).document(courseId)

            firestore.runTransaction { tx ->
                val acc = FirestoreAccumulator(tx)

                val activeDaysDelta = calculateActiveDaysDelta(tx, courseRef, newVideo, oldVideo)
                if (activeDaysDelta != 0L) {
                    acc.increment(courseRef, KEY_TOTAL_ACTIVE_DAYS, activeDaysDelta)
                    Log.d(TAG, "Active day increment: $activeDaysDelta")
                }

                // stats down for the original copy
                oldVideo?.let { applyStats(acc, courseRef, oldVideo, multiplier = -1L) }
                // stats up for the new copy
                applyStats(acc, courseRef, newVideo, multiplier = 1L)

                if (!channelExistsOnServer && channelMetadata != null) {
                    // only save if this is a new channel
                    val channelRef =
                        courseRef.collection(YT_CHANNEL_COLL).document(newVideo.channelId)
                    acc.setChannelMetadata(channelRef, channelMetadata)
                }

                acc.applyToTransaction()

                val videoRef = if (oldVideo != null && newVideo.id.isNotBlank()) {
                    courseRef.collection(YT_VIDEO_COLL).document(newVideo.id)
                } else {
                    courseRef.collection(YT_VIDEO_COLL).document()
                }
                tx.set(videoRef, newVideo.copy(id = videoRef.id))
            }.await()
        }
    }

    override suspend fun deleteYouTubeVideo(
        userId: String, courseId: String, video: YouTubeVideo
    ) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Deleting video ${video.title}")
            val courseRef = currentUserCourseColl(userId).document(courseId)

            firestore.runTransaction { tx ->
                val acc = FirestoreAccumulator(tx)
                val activeDaysDelta =
                    calculateActiveDaysDelta(tx, courseRef, newVideo = null, oldVideo = video)
                if (activeDaysDelta != 0L) {
                    acc.increment(courseRef, KEY_TOTAL_ACTIVE_DAYS, activeDaysDelta)
                    Log.d(TAG, "Active day increment: $activeDaysDelta")
                }

                // stats down
                applyStats(acc, courseRef, video, multiplier = -1L)
                acc.applyToTransaction()

                // delete video
                val videoRef = courseRef.collection(YT_VIDEO_COLL).document(video.id)
                tx.delete(videoRef)
            }.await()
        }
    }

    private fun calculateActiveDaysDelta(
        tx: com.google.firebase.firestore.Transaction,
        courseRef: DocumentReference,
        newVideo: YouTubeVideo?,
        oldVideo: YouTubeVideo?
    ): Long {
        if (newVideo != null && oldVideo != null && newVideo.watchedOn.toDayKey() == oldVideo.watchedOn.toDayKey()) {
            return 0L
        }
        var delta = 0L
        // lose the day when deleting the only video from it
        oldVideo?.let { if (getVideoDayCount(tx, courseRef, it) == 1L) delta -= 1L }
        // win a day when adding the first video to it
        newVideo?.let { if (getVideoDayCount(tx, courseRef, it) == 0L) delta += 1L }
        return delta
    }

    private fun getVideoDayCount(
        tx: com.google.firebase.firestore.Transaction,
        courseRef: DocumentReference,
        video: YouTubeVideo
    ): Long {
        val monthRef =
            courseRef.collection(USER_MONTHLY_STATS_COLL).document(video.watchedOn.toMonthKey())
        val snapshot = tx.get(monthRef)
        return snapshot.getLong("$KEY_DAYS.${video.watchedOn.toDayKey()}.$KEY_COUNT") ?: 0L
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
        acc.increment(courseRef, KEY_DURATION, dur)
        acc.increment(courseRef, KEY_COUNT, count)
        acc.updateTimestamp(courseRef, KEY_LAST_UPDATE)

        val channelRef = courseRef.collection(YT_CHANNEL_COLL).document(channelId)
        acc.increment(channelRef, KEY_DURATION, dur)
        acc.increment(channelRef, KEY_COUNT, count)
        acc.updateTimestamp(channelRef)

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


    private class FirestoreAccumulator(private val tx: com.google.firebase.firestore.Transaction) {
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
            val channelMap =
                channel.toFirestoreMap(excludeFields = setOf(KEY_DURATION, KEY_COUNT, KEY_ID))
            updates.putAll(channelMap)
        }

        fun updateTimestamp(ref: DocumentReference, field: String = KEY_TIMESTAMP) {
            val updates = documentUpdates.getOrPut(ref) { mutableMapOf() }
            updates[field] = FieldValue.serverTimestamp()
        }

        fun applyToTransaction() {
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
                    tx.set(ref, data, SetOptions.merge())
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