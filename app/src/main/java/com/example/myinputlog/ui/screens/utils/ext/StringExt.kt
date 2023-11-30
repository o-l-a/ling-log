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

package com.example.myinputlog.ui.screens.utils.ext

import android.util.Patterns
import java.util.regex.Pattern

private const val MIN_PASS_LENGTH = 8

// deleted the "non-white" character constraint
private const val PASS_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{4,}$"

fun String.isValidEmail(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.isNotBlank() &&
            this.length >= MIN_PASS_LENGTH &&
            Pattern.compile(PASS_PATTERN).matcher(this).matches()
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
