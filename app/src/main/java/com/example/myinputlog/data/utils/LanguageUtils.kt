package com.example.myinputlog.data.utils

object LanguageUtils {
    fun getLanguageDisplayName(languageCode: String?): String? {
        if (languageCode.isNullOrBlank()) return null
        val locale = java.util.Locale.forLanguageTag(languageCode)
        return locale.getDisplayName(java.util.Locale.getDefault())
    }
}