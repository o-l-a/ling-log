package com.example.myinputlog.ui.theme

import androidx.core.graphics.ColorUtils

object ColorHelpers {
    private const val CONTRAST_THRESHOLD = 0.45f
    private const val TARGET_SATURATION_DARK_BG = 0.65f
    private const val TARGET_SATURATION_LIGHT_BG = 0.50f
    private const val LIGHTNESS_TEXT_ON_DARK = 0.92f
    private const val LIGHTNESS_TEXT_ON_LIGHT = 0.15f

    fun calculateFontColor(bgArgb: Long): Long {
        val colorInt = bgArgb.toInt()
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(colorInt, hsl)

        val isDarkBg = ColorUtils.calculateLuminance(colorInt) < CONTRAST_THRESHOLD

        val outHsl = floatArrayOf(
            hsl[0],
            if (isDarkBg) hsl[1].coerceAtLeast(TARGET_SATURATION_DARK_BG) else hsl[1].coerceAtLeast(
                TARGET_SATURATION_LIGHT_BG
            ),
            if (isDarkBg) LIGHTNESS_TEXT_ON_DARK else LIGHTNESS_TEXT_ON_LIGHT
        )

        return (ColorUtils.HSLToColor(outHsl).toUInt() or 0xFF000000u).toLong()
    }

    fun longToHex(color: Long): String {
        return "%08X".format(color and 0xFFFFFFFFL)
    }

    fun hexToLong(hex: String): Long? {
        val cleaned = hex.removePrefix("#").trim()
        return try {
            when (cleaned.length) {
                6 -> "FF$cleaned".toLong(16)
                8 -> cleaned.toLong(16)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}