package com.example.myinputlog.data.service.impl

import android.util.Log
import com.example.myinputlog.data.model.UserData
import com.example.myinputlog.data.service.AccountService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

const val TAG = "AccountService"

class DefaultAccountService @Inject constructor(
    private val auth: FirebaseAuth
) : AccountService {
    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val currentUser: Flow<UserData>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let {
                        it.displayName?.let { it1 ->
                            UserData(
                                it.uid,
                                it1
                            )
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
        val deferred = CompletableDeferred<Unit>()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                    }
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { task1 ->
                        if (task1.isSuccessful) {
                            Log.d(TAG, "updateProfile:success")
                        } else {
                            Log.d(TAG, "createUser:abort")
                            user.delete()
                            deferred.completeExceptionally(
                                task.exception ?: Exception("Unknown exception")
                            )
                        }
                    }
                    deferred.complete(Unit)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    deferred.completeExceptionally(task.exception ?: Exception("Unknown exception"))
                }
            }
        deferred.await()
    }

    override suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}