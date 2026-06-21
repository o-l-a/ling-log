package com.example.myinputlog.data.service.impl

import android.util.Log
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.model.ChannelWithLabelIds
import com.example.myinputlog.data.local.model.VideoWithLabelIds
import com.example.myinputlog.data.local.toFirestoreMap
import com.example.myinputlog.data.remote.dto.AppConfigDto
import com.example.myinputlog.data.remote.dto.ChannelDto
import com.example.myinputlog.data.remote.dto.CountryGroupDto
import com.example.myinputlog.data.remote.dto.CourseDto
import com.example.myinputlog.data.remote.dto.LabelDto
import com.example.myinputlog.data.remote.dto.LabelDtoWrapper
import com.example.myinputlog.data.remote.dto.SyncPointersDto
import com.example.myinputlog.data.remote.dto.VideoDto
import com.example.myinputlog.data.remote.dto.VideoDtoWrapper
import com.example.myinputlog.data.service.StorageService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class DefaultStorageService @Inject constructor(
    private val firestore: FirebaseFirestore
) : StorageService {

    companion object {
        private const val TAG = "VideoStorageService"

        private const val COLL_USERS = "users"
        private const val COLL_METADATA = "metadata"
        private const val COLL_COURSES = "courses"
        private const val COLL_CHANNELS = "channels"
        private const val COLL_MONTHS = "months"

        private const val DOC_SYNC_POINTERS = "syncPointers"
        private const val DOC_LABELS = "labels"
        private const val DOC_APP_CONFIG = "app_config"

        private const val FIELD_LAST_UPDATED = "lastUpdated"
        private const val FIELD_LABELS_LAST_UPDATED = "labelsLastUpdated"
        private const val FIELD_CHANNELS_LAST_UPDATED = "channelsLastUpdated"
        private const val FIELD_COURSES_LAST_UPDATED = "coursesLastUpdated"
        private const val FIELD_VIDEOS = "videos"
        private const val FIELD_LABELS = "labels"

        private const val CHUNK_SIZE = 499
    }

    private fun userRef(uid: String): DocumentReference =
        firestore.collection(COLL_USERS).document(uid)

    private fun courseRef(uid: String): CollectionReference = userRef(uid).collection(COLL_COURSES)

    private fun metadataRef(uid: String): CollectionReference =
        userRef(uid).collection(COLL_METADATA)

    private fun monthRef(uid: String): CollectionReference = userRef(uid).collection(COLL_MONTHS)

    private fun channelRef(uid: String): CollectionReference =
        userRef(uid).collection(COLL_CHANNELS)

    private fun appConfigRef(): DocumentReference =
        firestore.collection(COLL_METADATA).document(DOC_APP_CONFIG)

    override suspend fun pushMonths(userId: String, months: Map<String, List<VideoWithLabelIds>>) {
        val batch = firestore.batch()
        for ((monthKey, videos) in months) {
            val docRef = monthRef(userId).document(monthKey)
            val videosMap = videos.associate { video ->
                video.video.id to video.toFirestoreMap()
            }
            val updates = mapOf(
                FIELD_VIDEOS to videosMap, FIELD_LAST_UPDATED to FieldValue.serverTimestamp()
            )
            batch.set(docRef, updates, SetOptions.merge())
        }
        batch.commit().await()
    }

    override suspend fun pushChannels(
        userId: String, channels: List<ChannelWithLabelIds>
    ) {
        val pointersRef = metadataRef(userId).document(DOC_SYNC_POINTERS)
        channels.chunked(CHUNK_SIZE).forEach { chunk ->
            val batch = firestore.batch()
            val pointersUpdate = mutableMapOf<String, Any>()
            chunk.forEach { channel ->
                val docRef = channelRef(userId).document(channel.channel.id)
                batch.set(docRef, channel.toFirestoreMap())
            }
            pointersUpdate[FIELD_CHANNELS_LAST_UPDATED] = FieldValue.serverTimestamp()
            batch.set(pointersRef, pointersUpdate, SetOptions.merge())
            batch.commit().await()
        }
    }

    override suspend fun pushMetadata(
        userId: String, courses: List<CourseEntity>, labels: List<LabelEntity>
    ) {
        val batch = firestore.batch()
        val pointersUpdate = mutableMapOf<String, Any>()

        if (courses.isNotEmpty()) {
            courses.forEach { course ->
                val docRef = courseRef(userId).document(course.id)
                val data = course.toFirestoreMap()
                batch.set(docRef, data, SetOptions.merge())
            }
            pointersUpdate[FIELD_COURSES_LAST_UPDATED] = FieldValue.serverTimestamp()
        } else {
            Log.d(TAG, "No courses to update. Skipped.")
        }

        if (labels.isNotEmpty()) {
            val docRef = metadataRef(userId).document(DOC_LABELS)
            val labelsMap = labels.associate { label ->
                label.id to label.toFirestoreMap()
            }

            val updates = mapOf(
                FIELD_LABELS to labelsMap, FIELD_LAST_UPDATED to FieldValue.serverTimestamp()
            )

            batch.set(docRef, updates, SetOptions.merge())

            pointersUpdate[FIELD_LABELS_LAST_UPDATED] = FieldValue.serverTimestamp()
        } else {
            Log.d(TAG, "No labels to update. Skipped.")
        }

        if (pointersUpdate.isNotEmpty()) {
            val pointersRef = metadataRef(userId).document(DOC_SYNC_POINTERS)
            batch.set(pointersRef, pointersUpdate, SetOptions.merge())
        } else {
            Log.d(TAG, "No sync pointers to update. Skipped.")
        }

        batch.commit().await()
    }

    override suspend fun getSyncPointers(userId: String): SyncPointersDto? {
        return metadataRef(userId).document(DOC_SYNC_POINTERS).get().await()
            .toObject(SyncPointersDto::class.java)
    }

    override suspend fun getLastUpdatedCourses(
        userId: String, lastPull: Date
    ): List<CourseDto> {
        return courseRef(userId).whereGreaterThan(FIELD_LAST_UPDATED, Timestamp(lastPull)).get()
            .await().toObjects(CourseDto::class.java)
    }

    override suspend fun getLastUpdatedLabels(
        userId: String
    ): List<LabelDto> {
        return metadataRef(userId).document(DOC_LABELS).get().await()
            .toObject(LabelDtoWrapper::class.java)?.labels?.map { it.value } ?: emptyList()
    }

    override suspend fun getLastUpdatedVideos(
        userId: String, lastPull: Date
    ): List<VideoDto> {
        Log.d(
            TAG, "Pulling videos with timestamp: ${Timestamp(lastPull).toDate()}"
        )
        val months =
            monthRef(userId).whereGreaterThan(FIELD_LAST_UPDATED, Timestamp(lastPull)).get().await()
                .toObjects(
                    VideoDtoWrapper::class.java
                )
        Log.d(TAG, "Found months: $months")
        return months.flatMap { monthDoc ->
            monthDoc.videos?.map {
                it.value
            } ?: emptyList()
        }
    }

    override suspend fun getLastUpdatedChannels(
        userId: String, lastPull: Date
    ): List<ChannelDto> {
        return channelRef(userId).whereGreaterThan(FIELD_LAST_UPDATED, Timestamp(lastPull)).get()
            .await().toObjects(ChannelDto::class.java)
    }

    override suspend fun getLastUpdatedCountryGroups(lastPull: Date): List<CountryGroupDto> {
        try {
            return appConfigRef().get(Source.SERVER).await().toObject(AppConfigDto::class.java)
                ?.takeIf { it.lastUpdated != null && it.lastUpdated > Timestamp(lastPull) }?.countryGroups?.map {
                    it.value.copy(id = it.key)
                } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Pull failed: ${e.message}")
            return emptyList()
        }
    }


    override suspend fun initializeUser(uid: String) {
        userRef(uid).set(hashMapOf<String, Any>()).await()
        Log.d(TAG, "createCollection:success")
    }

    override suspend fun deleteAllForUser(
        userId: String, courseIds: List<String>, channelIds: List<String>, monthKeys: List<String>
    ) {
        val refsToDelete = mutableListOf<DocumentReference>()

        refsToDelete.add(metadataRef(userId).document(DOC_LABELS))
        refsToDelete.add(metadataRef(userId).document(DOC_SYNC_POINTERS))

        courseIds.forEach { id ->
            refsToDelete.add(courseRef(userId).document(id))
        }

        channelIds.forEach { id ->
            refsToDelete.add(channelRef(userId).document(id))
        }

        monthKeys.forEach { key ->
            refsToDelete.add(monthRef(userId).document(key))
        }

        refsToDelete.add(userRef(userId))

        refsToDelete.chunked(CHUNK_SIZE).forEach { chunk ->
            firestore.runBatch { batch ->
                chunk.forEach { ref ->
                    batch.delete(ref)
                }
            }.await()
        }

        Log.d(TAG, "Deleted ${refsToDelete.size} documents.")
    }
}