package com.example.myinputlog.ui.screens.utils

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import com.example.myinputlog.BuildConfig
import com.example.myinputlog.R
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

const val PAGE_SIZE = 10
const val MAX_PAGE_SIZE = 1024

val IME_ACTION_NEXT = KeyboardOptions.Default.copy(
    imeAction = ImeAction.Next
)

val IME_ACTION_DONE = KeyboardOptions.Default.copy(
    imeAction = ImeAction.Done
)

@SuppressLint("SimpleDateFormat")
val dateFormatter = SimpleDateFormat("dd/MM/yyyy")

val languageMap = mapOf(
    "en" to "English",
    "es" to "Spanish",
    "es-419" to "Spanish (Latin America)",
    "pl" to "Polish",
    "fr" to "French",
    "de" to "German",
    "it" to "Italian",
    "pt" to "Portuguese",
    "ru" to "Russian",
    "zh" to "Chinese",
    "ja" to "Japanese",
    "ko" to "Korean",
    "hi" to "Hindi",
    "ar" to "Arabic",
    "vi" to "Vietnamese",
    "nl" to "Dutch"
)

fun getLanguageName(languageCode: String): String {
    return languageMap[languageCode] ?: "Unknown Language"
}

fun formatDuration(duration: Long): String {
    val hours = TimeUnit.SECONDS.toHours(duration)
    val minutes = TimeUnit.SECONDS.toMinutes(duration - TimeUnit.HOURS.toSeconds(hours))
    val seconds = duration - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes)

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun formatDurationAsText(duration: Long): String {
    val hours = TimeUnit.SECONDS.toHours(duration)
    val minutes = TimeUnit.SECONDS.toMinutes(duration - TimeUnit.HOURS.toSeconds(hours))

    return if (hours > 99) {
        "${hours}h"
    } else if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}

@Composable
fun myInputLogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    errorBorderColor = Color.Transparent
)

enum class Country(@StringRes val countryNameResId: Int, val flagEmoji: String) {
    SPAIN(R.string.country_spain, "🇪🇸"),
    MEXICO(R.string.country_mexico, "🇲🇽"),
    ARGENTINA(R.string.country_argentina, "🇦🇷"),
    COLOMBIA(R.string.country_colombia, "🇨🇴"),
    CHILE(R.string.country_chile, "🇨🇱"),
    VENEZUELA(R.string.country_venezuela, "🇻🇪"),
    PERU(R.string.country_peru, "🇵🇪"),
    ECUADOR(R.string.country_ecuador, "🇪🇨"),
    CUBA(R.string.country_cuba, "🇨🇺"),
    DOMINICAN_REPUBLIC(R.string.country_dominican_republic, "🇩🇴"),
    PUERTO_RICO(R.string.country_puerto_rico, "🇵🇷"),
    URUGUAY(R.string.country_uruguay, "🇺🇾"),
    PARAGUAY(R.string.country_paraguay, "🇵🇾"),
    BOLIVIA(R.string.country_bolivia, "🇧🇴"),
}

class AuthConstants {
    companion object {
        const val SHARED_PREFERENCES_NAME = "AUTH_STATE_PREFERENCE"
        const val AUTH_STATE = "AUTH_STATE"

        const val SCOPE_PROFILE = "profile"
        const val SCOPE_EMAIL = "email"
        const val SCOPE_OPENID = "openid"
        const val SCOPE_YOUTUBE = "https://www.googleapis.com/auth/youtube"
        const val SCOPE_GDATA_YOUTUBE = "https://www.gdata.youtube.com"
        const val SCOPE_GDATA_YOUTUBE_SLASH = "https://gdata.youtube.com/"
        const val SCOPE_GDATA_YOUTUBE_NO_SSL = "http://gdata.youtube.com"
        const val SCOPE_GDATA_YOUTUBE_SLASH_NO_SSL = "http://gdata.youtube.com/"
        const val SCOPE_GDATA_YOUTUBE_FEEDS = "https://gdata.youtube.com/feeds/"
        const val SCOPE_GDATA_YOUTUBE_VIDEO_API = "http://gdata.youtube.com/feeds/api/videos/"
        const val SCOPE_GDATA_YOUTUBE_USER_PLAYLISTS = "http://gdata.youtube.com/feeds/api/users/default/playlists"
        const val SCOPE_GDATA_YOUTUBE_OTHER = "http://gdata.youtube.com/youtube"
        const val SCOPE_GDATA_YOUTUBE_USER_FAVORITES = "https://gdata.youtube.com/feeds/api/users/default/favorites/"
        const val SCOPE_GDATA_YOUTUBE_API = "https://gdata.youtube.com/feeds/api"
        const val SCOPE_GDATA_YOUTUBE_CAPTIONS = "https://gdata.youtube.com/captions"
        const val SCOPE_GDATA_YOUTUBE_FEED = "https://gdata.youtube.com/feed"
        const val SCOPE_YOUTUBE_PARTNER = "https://www.googleapis.com/auth/youtubepartner"
        const val SCOPE_YOUTUBE_PARTNER_CHANNEL_AUDIT = "https://www.googleapis.com/auth/youtubepartner-channel-audit"
        const val SCOPE_YOUTUBE_READONLY = "https://www.googleapis.com/auth/youtube.readonly"
        const val SCOPE_YOUTUBE_FORCE_SSL = "https://www.googleapis.com/auth/youtube.force-ssl"


        const val DATA_PICTURE = "picture"
        const val DATA_FIRST_NAME = "given_name"
        const val DATA_LAST_NAME = "family_name"
        const val DATA_EMAIL = "email"

        const val CLIENT_ID = BuildConfig.CLIENT_ID
        const val CODE_VERIFIER_CHALLENGE_METHOD = "S256"
        const val MESSAGE_DIGEST_ALGORITHM = "SHA-256"

        const val URL_AUTHORIZATION = "https://accounts.google.com/o/oauth2/v2/auth"
        const val URL_TOKEN_EXCHANGE = "https://www.googleapis.com/oauth2/v4/token"
        const val URL_AUTH_REDIRECT = "com.example.myinputlog:/oauth2redirect"
        const val URL_LOGOUT = "https://oauth2.googleapis.com/revoke"
    }
}