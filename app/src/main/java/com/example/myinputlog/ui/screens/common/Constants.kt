package com.example.myinputlog.ui.screens.common

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import com.example.myinputlog.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

const val PAGE_SIZE = 8
const val MAX_PAGE_SIZE = 1024
const val DEFAULT_INITIAL_PAGE_MULTIPLIER = 1.5

const val MAX_LABEL_LENGTH = 50
const val MAX_COURSE_LENGTH = 120
const val MAX_USER_LENGTH = 50
const val MAX_URL_LENGTH = 120


val IME_ACTION_NEXT = KeyboardOptions.Default.copy(
    imeAction = ImeAction.Next
)

val IME_ACTION_DONE = KeyboardOptions.Default.copy(
    imeAction = ImeAction.Done
)

@SuppressLint("SimpleDateFormat")
val dateFormatter = SimpleDateFormat("dd/MM/yyyy")

fun formatDuration(duration: Long): String {
    val hours = TimeUnit.SECONDS.toHours(duration)
    val minutes = TimeUnit.SECONDS.toMinutes(duration - TimeUnit.HOURS.toSeconds(hours))
    val seconds = duration - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes)

    return if (hours > 0) {
        String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
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

enum class ConfettiOptions(@get:StringRes val optionName: Int, val colors: List<Long>) {
    OPTION1(
        R.string.confetti_option_1, listOf(0xFFfce18a, 0xFFff726d, 0xFFf4306d, 0xFFb48def)
    ),
    OPTION2(
        R.string.confetti_option_2,
        listOf(0xFFE40303, 0xFFFF8C00, 0xFFFFED00, 0xFF008026, 0xFF004CFF, 0xFF732982)
    ),
    OPTION3(
        R.string.confetti_option_3,
        listOf(0xFFD52D00, 0xFFEF7627, 0xFFFF9A56, 0xFFFFFFFF, 0xFFD162A4, 0xFFB55690, 0xFFA30262)
    ),
    OPTION4(
        R.string.confetti_option_4, listOf(0xFF5BCEFA, 0xFFFFFFFF, 0xFFF5A9B8)
    ),
    OPTION5(
        R.string.confetti_option_5, listOf(0xFFD60270, 0xFF9B4F96, 0xFF0038A8)
    ),
    OPTION6(
        R.string.confetti_option_6, listOf(0xFF000000, 0xFFA3A3A3, 0xFFFFFFFF, 0xFF800080)
    ),
    OPTION7(R.string.confetti_option_7, listOf(0xFFFF218C, 0xFFFFD800, 0xFF21B1FF))
}