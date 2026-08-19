package com.example.myinputlog.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import com.materialkolor.hct.Hct

object ColorHelpers {
    private const val CONTRAST_THRESHOLD = 0.45f
    private const val TARGET_SATURATION_DARK_BG = 0.65f
    private const val TARGET_SATURATION_LIGHT_BG = 0.50f
    private const val LIGHTNESS_TEXT_ON_DARK = 0.92f
    private const val LIGHTNESS_TEXT_ON_LIGHT = 0.15f

    fun calculateFontColors(bgArgb: List<Long>): Long {
        if (bgArgb.isEmpty()) return 0xFF000000L

        val colorInt = if (bgArgb.size > 1) {
            val hctList = bgArgb.map { Hct.fromInt(it.toInt()) }
            hctList.map { it.tone }.average().toInt()
        } else {
            bgArgb.first().toInt()
        }
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

    fun calculateFontColor(bgArgb: Long): Long = calculateFontColors(listOf(bgArgb))

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

    fun getCompensatedGradientBrush(colors: List<Color>): Brush {
        if (colors.isEmpty()) return Brush.linearGradient(
            listOf(
                Color.Transparent, Color.Transparent
            )
        )
        if (colors.size == 1) return Brush.linearGradient(listOf(colors.first(), colors.first()))

        val stops = mutableListOf<Pair<Float, Color>>()
        val n = colors.size

        colors.forEachIndexed { i, color ->
            val position = i.toFloat() / (n - 1)
            when (i) {
                0 -> {
                    stops.add(0.0f to color)
                    stops.add(0.12f to color)
                }

                n - 1 -> {
                    stops.add(0.88f to color)
                    stops.add(1.0f to color)
                }

                else -> {
                    stops.add(position to color)
                }
            }
        }

        return Brush.linearGradient(
            colorStops = stops.toTypedArray(),
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }
}