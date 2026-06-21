package com.example.myinputlog.ui.models

import java.util.Locale

data class CountryUiModel(
    val isoCode: String, val displayName: String, val flagEmoji: String
)


fun String.toCountryUiModel(): CountryUiModel {
    val code = this.trim().uppercase()

    return CountryUiModel(
        isoCode = code,
        displayName = Locale.Builder().setRegion(code).build().displayCountry,
        flagEmoji = code.toFlagEmoji()
    )
}

private fun String.toFlagEmoji(): String {
    if (this.length != 2) return "🏳️"

    val offset = 127397
    val firstChar = Character.codePointAt(this, 0) + offset
    val secondChar = Character.codePointAt(this, 1) + offset

    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}