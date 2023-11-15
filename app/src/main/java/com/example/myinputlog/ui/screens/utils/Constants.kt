package com.example.myinputlog.ui.screens.utils

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import com.example.myinputlog.R
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

const val PAGE_SIZE = 3
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
