package com.example.myinputlog.ui.models

import android.util.Log
import java.util.Locale

data class CountryUiModel(
    val isoCode: String, val displayName: String, val flagEmoji: String
) {
    companion object {
        fun unknown(code: String = "??") = CountryUiModel(
            isoCode = code, displayName = "Unknown ($code)", flagEmoji = "🏳️"
        )
    }
}


fun String?.toCountryUiModel(): CountryUiModel {
    if (this.isNullOrBlank()) return CountryUiModel.unknown()

    val code = this.trim().uppercase()
    if (code.length != 2 || !code.all { it in 'A'..'Z' }) {
        return CountryUiModel.unknown(code)
    }

    return try {
        val locale = Locale.Builder().setRegion(code).build()
        val name = locale.getDisplayCountry(Locale.getDefault())

        val finalName = if (name.isNullOrEmpty() || name == code) {
            "Unknown Country ($code)"
        } else {
            name
        }

        CountryUiModel(
            isoCode = code, displayName = finalName, flagEmoji = code.toFlagEmoji()
        )
    } catch (e: Exception) {
        Log.d("CountryModel", e.toString())
        CountryUiModel.unknown(code)
    }
}

fun String?.toCountryUiModelOrNull(): CountryUiModel? {
    if (this.isNullOrBlank()) return null
    return this.toCountryUiModel()
}

private fun String.toFlagEmoji(): String {
    if (this.length != 2) return "🏳️"

    val offset = 127397
    val firstChar = Character.codePointAt(this, 0) + offset
    val secondChar = Character.codePointAt(this, 1) + offset

    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}