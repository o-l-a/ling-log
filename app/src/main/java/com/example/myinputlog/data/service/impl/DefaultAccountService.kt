package com.example.myinputlog.data.service.impl

import android.util.Log
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.data.model.YouTubeVideo
import com.example.myinputlog.data.service.AccountService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DefaultAccountService @Inject constructor(
    private val auth: FirebaseAuth,
    val firestore: FirebaseFirestore,
) : AccountService {
    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val currentUser: Flow<UserData>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { user ->
                        user.displayName?.let { username ->
                            UserData(user.uid, username, user.email ?: "")
                        }
                    } ?: UserData())
                }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    override suspend fun signIn(email: String, password: String) {
        val deferred = CompletableDeferred<Unit>()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    deferred.complete(Unit)
                } else {
                    Log.d(TAG, "signIn:failure")
                    deferred.completeExceptionally(task.exception ?: Exception("Unknown exception"))
                }
            }
        deferred.await()
    }

    override suspend fun createAccount(email: String, password: String, username: String) {
        try {
            val userCredential = auth.createUserWithEmailAndPassword(email, password).await()
            val user = userCredential.user

            if (user != null) {
                Log.d(TAG, "createUserWithEmail:success")
                val profileUpdates = userProfileChangeRequest {
                    displayName = username
                }
                user.updateProfile(profileUpdates).await()
                Log.d(TAG, "updateProfile:success ${user.displayName}")

                val firestoreTask =
                    firestore.collection(DefaultStorageService.USER_COLLECTION).document(user.uid)
                        .set({})
                firestoreTask.await()
                Log.d(TAG, "createCollection:success")
            } else {
                Log.e(TAG, "createAccount: User is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "createAccount:failure", e)
            throw e
        }
    }

    override suspend fun deleteAccount() {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            try {
                deleteAllForUser(uid)
                firestore.collection(DefaultStorageService.USER_COLLECTION).document(uid).delete()
                    .await()
                auth.currentUser?.delete()?.await()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting account and document: ${e.message}")
                throw e
            }
        }
    }

    override suspend fun deleteAllForUser(userId: String) {
        val courseCollection = firestore
            .collection(DefaultStorageService.USER_COLLECTION)
            .document(userId)
            .collection(DefaultStorageService.USER_COURSE_COLLECTION)
        val courses: List<UserCourse> = courseCollection
            .get()
            .await()
            .toObjects()
        courses.forEach {
            deleteAllVideosForCourse(userId, it.id)
            courseCollection.document(it.id).delete().await()
        }
    }

    override suspend fun deleteAllVideosForCourse(userId: String, userCourseId: String) {
        val videosCollection = firestore
            .collection(DefaultStorageService.USER_COLLECTION)
            .document(userId)
            .collection(DefaultStorageService.USER_COURSE_COLLECTION)
            .document(userCourseId)
            .collection(DefaultStorageService.YOU_TUBE_VIDEO_COLLECTION)
        val videosForCourse: List<YouTubeVideo> = videosCollection
            .get()
            .await()
            .toObjects()
        videosForCourse.forEach {
            videosCollection.document(it.id).delete().await()
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun changeUsername(newUsername: String) {
        try {
            val user = auth.currentUser
            if (user != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = newUsername
                }
                user.updateProfile(profileUpdates).await()
                Log.d(TAG, "Username updated to ${user.displayName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating username: ${e.message}")
            throw e
        }
    }

    companion object {
        private const val TAG = "AccountService"
    }
}