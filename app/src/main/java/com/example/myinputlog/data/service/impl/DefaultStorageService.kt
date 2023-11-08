package com.example.myinputlog.data.service.impl

import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.StorageService
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.google.firebase.perf.trace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun videos(courseId: String): Flow<List<YouTubeVideo>> {
        return auth.currentUser.flatMapLatest { user ->
            youTubeVideoCollectionForCurrentUserCourse(user.id, courseId).snapshots()
                .map { snapshot -> snapshot.toObjects() }
        }
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

    override suspend fun deleteAllForUser(userId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllVideosForCourse(userCourseId: String) {
        TODO("Not yet implemented")
    }

    companion object {
        const val USER_COLLECTION = "users"
        private const val USER_COURSE_COLLECTION = "userCourses"
        private const val USER_COURSE_SAVE_TRACE = "saveUserCourse"
        private const val USER_COURSE_UPDATE_TRACE = "updateUserCourse"
        private const val YOU_TUBE_VIDEO_COLLECTION = "youTubeVideos"
        private const val YOU_TUBE_VIDEO_SAVE_TRACE = "saveYouTubeVideo"
        private const val YOU_TUBE_VIDEO_UPDATE_TRACE = "updateYouTubeVideo"
    }
}