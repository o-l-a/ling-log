package com.example.myinputlog.data.service

import com.example.myinputlog.data.model.UserData
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUserId: String
    val currentUser: Flow<UserData>

    suspend fun signIn(email: String, password: String)
    suspend fun createAccount(email: String, password: String, username: String)
    suspend fun deleteAccount()
    suspend fun signOut()
}
