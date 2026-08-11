/*
Copyright 2022 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.example.myinputlog.ui.screens.common.ext

import android.util.Patterns
import java.util.regex.Pattern

private const val MIN_PASS_LENGTH = 8

// deleted the "non-white" character constraint
private const val PASS_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{4,}$"
private val passPatternCompiled = Pattern.compile(PASS_PATTERN)

private const val URL_PATTERN =
    "http(?:s)?://(?:m\\.)?(?:www\\.)?youtu(?:\\.be/|(?:be-nocookie|be)\\.com/(?:watch|[\\w]+\\?(?:feature=[\\w]+\\.[\\w]+\\&)?v=|v/|e/|embed/|live/|shorts/|user/(?:[\\w#]+/)+))([^&#?\\n]+)"
private val urlPatternCompiled = Pattern.compile(URL_PATTERN)


fun String.isValidEmail(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.isNotBlank() && this.length >= MIN_PASS_LENGTH && passPatternCompiled.matcher(this)
        .matches()
}

fun String.passwordMatches(repeated: String): Boolean {
    return this == repeated
}

// added a new extension
fun String.isValidUsername(): Boolean {
    return this.isNotBlank()
}

fun String.toNonNegativeLongOrNull(): Long? {
    val parsedValue = this.toLongOrNull()
    return if (parsedValue != null && parsedValue >= 0) parsedValue else null
}

fun String.hideEmail(): String {
    val atIndex = indexOf('@')
    return buildString {
        if (atIndex != -1) {
            append(this@hideEmail[0])
            append("*".repeat(atIndex - 1))
            append(this@hideEmail.substring(atIndex))
        } else {
            append(this@hideEmail)
        }
    }
}

fun String.extractYouTubeVideoId(): String? {
    val matcher = urlPatternCompiled.matcher(this)
    return if (matcher.find()) {
        matcher.group(1)
    } else null
}

fun String.stripUrl(): String {
    return this.substringBefore('?')
}