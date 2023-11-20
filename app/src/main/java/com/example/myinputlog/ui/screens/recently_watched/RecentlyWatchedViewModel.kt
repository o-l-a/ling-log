package com.example.myinputlog.ui.screens.recently_watched

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.JWT
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.service.ApiService
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.screens.utils.AuthConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class RecentlyWatchedViewModel @Inject constructor(
    application: Application,
    storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService,
    private val authorizationServiceConfiguration: AuthorizationServiceConfiguration,
    private val authorizationService: AuthorizationService,
    private val apiService: ApiService
) : AndroidViewModel(application) {
    private val _recentlyWatchedUiState = MutableStateFlow(RecentlyWatchedUiState())
    val recentlyWatchedUiState = _recentlyWatchedUiState.asStateFlow()
    private val userCourses = storageService.userCourses

    private var authState: AuthState = AuthState()
    private var jwt: JWT? = null

    init {
        restoreState()
        viewModelScope.launch {
            val currentCourse = userCourses.firstOrNull()?.find {
                it.id == (preferenceStorageService.currentCourseId.firstOrNull() ?: "")
            } ?: UserCourse()
            try {
                _recentlyWatchedUiState.update {
                    currentCourse.toRecentlyWatchedUiState().copy(
                        isLoading = false
                    )
                }
                updateChannel()
            } catch (e: Exception) {
                e.message?.let { Log.d(TAG, it) }
            }
        }
    }

    private fun updateChannel() {
        if (jwt != null) {
            val email = Date((jwt?.getClaim("exp")?.asLong()?.times(1000)) ?: 0).toString()
            val givenName = jwt?.getClaim(AuthConstants.DATA_FIRST_NAME)?.asString() ?: "no name"
            val familyName = Date(jwt?.getClaim("iat")?.asLong()?.times(1000) ?: 0).toString()
            val pictureUrl = jwt?.getClaim(AuthConstants.DATA_PICTURE)?.asString() ?: "no picture"
            _recentlyWatchedUiState.update {
                it.copy(
                    channelEmail = email,
                    channelPictureUrl = pictureUrl,
                    channelGivenName = givenName,
                    channelFamilyName = familyName
                )
            }
        }
    }

    private fun restoreState() {
        val jsonString = getApplication<Application>()
            .getSharedPreferences(AuthConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(AuthConstants.AUTH_STATE, null)
        if (jsonString != null && !TextUtils.isEmpty(jsonString)) {
            try {
                authState = AuthState.jsonDeserialize(jsonString)
                if (!TextUtils.isEmpty(authState.idToken)) {
                    jwt = JWT(authState.idToken!!)
                }
            } catch (jsonException: JSONException) {
                Log.d(TAG, jsonException.message.toString())
            }
        }
        updateChannel()
    }

    private fun persistState() {
        getApplication<Application>().getSharedPreferences(
            AuthConstants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(AuthConstants.AUTH_STATE, authState.jsonSerializeString())
            .apply()
        updateChannel()
    }

    fun attemptAuthorization(): Intent {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)

        val encoding = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        val codeVerifier = Base64.encodeToString(bytes, encoding)

        val digest = MessageDigest.getInstance(AuthConstants.MESSAGE_DIGEST_ALGORITHM)
        val hash = digest.digest(codeVerifier.toByteArray())
        val codeChallenge = Base64.encodeToString(hash, encoding)

        val builder = AuthorizationRequest.Builder(
            authorizationServiceConfiguration,
            AuthConstants.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(AuthConstants.URL_AUTH_REDIRECT)
        )
            .setCodeVerifier(
                codeVerifier,
                codeChallenge,
                AuthConstants.CODE_VERIFIER_CHALLENGE_METHOD
            )

        builder.setScopes(
            AuthConstants.SCOPE_PROFILE,
            AuthConstants.SCOPE_EMAIL,
            AuthConstants.SCOPE_OPENID,
            AuthConstants.SCOPE_YOUTUBE,
            AuthConstants.SCOPE_GDATA_YOUTUBE,
            AuthConstants.SCOPE_GDATA_YOUTUBE_SLASH,
            AuthConstants.SCOPE_GDATA_YOUTUBE_NO_SSL,
            AuthConstants.SCOPE_GDATA_YOUTUBE_SLASH_NO_SSL,
            AuthConstants.SCOPE_GDATA_YOUTUBE_FEEDS,
            AuthConstants.SCOPE_GDATA_YOUTUBE_VIDEO_API,
            AuthConstants.SCOPE_GDATA_YOUTUBE_USER_PLAYLISTS,
            AuthConstants.SCOPE_GDATA_YOUTUBE_OTHER,
            AuthConstants.SCOPE_GDATA_YOUTUBE_USER_FAVORITES,
            AuthConstants.SCOPE_GDATA_YOUTUBE_API,
            AuthConstants.SCOPE_GDATA_YOUTUBE_CAPTIONS,
            AuthConstants.SCOPE_GDATA_YOUTUBE_FEED,
            AuthConstants.SCOPE_YOUTUBE_PARTNER,
            AuthConstants.SCOPE_YOUTUBE_PARTNER_CHANNEL_AUDIT,
            AuthConstants.SCOPE_YOUTUBE_READONLY,
            AuthConstants.SCOPE_YOUTUBE_FORCE_SSL
        )

        val request = builder.build()
        return authorizationService.getAuthorizationRequestIntent(request)
    }

    fun handleAuthorizationResponse(intent: Intent) {
        val authorizationResponse: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)

        authState = AuthState(authorizationResponse, error)
        if (authorizationResponse != null) {
            val tokenExchangeRequest = authorizationResponse.createTokenExchangeRequest()
            authorizationService.performTokenRequest(tokenExchangeRequest) { response, exception ->
                if (exception != null) {
                    authState = AuthState()
                    Log.d(TAG, exception.message.toString())
                } else {
                    Log.d(TAG, response.toString())
                    if (response != null) {
                        authState.update(response, exception)
                        jwt = JWT(response.idToken!!)

                    }
                }
                persistState()
            }
        } else {
            Log.d(TAG, error?.message.toString())
        }
    }

    fun signOutWithoutRedirect() {
        Log.d(TAG, "token: $jwt")
        viewModelScope.launch {
            Log.d(TAG, "Begin sign out")
            val client = OkHttpClient()
            val formBody = FormBody.Builder()
                .add("token", authState.idToken ?: "")
                .build()

            val request = Request.Builder()
                .url(AuthConstants.URL_LOGOUT)
                .post(formBody)
                .build()
            try {
                Log.d(TAG, "Begin call client")
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d(TAG, e.message.toString())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d(TAG, "Response: $response")
                        if (response.isSuccessful) {
                            authState = AuthState()
                            jwt = null
                            updateChannel()
                        }
                    }
                })
            } catch (e: IOException) {
                Log.d(TAG, e.message.toString())
            }
        }
    }

    fun getVideos() {
        authState.performActionWithFreshTokens(
            authorizationService
        ) { _, _, _ ->
            viewModelScope.launch {
                try {
                    val response =
                        apiService.getMyChannelInfo(token = "Bearer ${authState.accessToken!!}")
                    Log.d(TAG, response.toString())
                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                }
            }
        }
    }

    companion object {
        private const val TAG = "RecentlyWatchedViewModel"
    }
}