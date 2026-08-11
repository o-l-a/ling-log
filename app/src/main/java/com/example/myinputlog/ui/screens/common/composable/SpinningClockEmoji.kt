package com.example.myinputlog.ui.screens.common.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.example.myinputlog.R
import java.time.LocalTime

private val clockFrames = listOf(
    R.drawable.img_emoji_clock_0000,
    R.drawable.img_emoji_clock_0030,
    R.drawable.img_emoji_clock_0100,
    R.drawable.img_emoji_clock_0130,
    R.drawable.img_emoji_clock_0200,
    R.drawable.img_emoji_clock_0230,
    R.drawable.img_emoji_clock_0300,
    R.drawable.img_emoji_clock_0330,
    R.drawable.img_emoji_clock_0400,
    R.drawable.img_emoji_clock_0430,
    R.drawable.img_emoji_clock_0500,
    R.drawable.img_emoji_clock_0530,
    R.drawable.img_emoji_clock_0600,
    R.drawable.img_emoji_clock_0630,
    R.drawable.img_emoji_clock_0700,
    R.drawable.img_emoji_clock_0730,
    R.drawable.img_emoji_clock_0800,
    R.drawable.img_emoji_clock_0830,
    R.drawable.img_emoji_clock_0900,
    R.drawable.img_emoji_clock_0930,
    R.drawable.img_emoji_clock_1000,
    R.drawable.img_emoji_clock_1030,
    R.drawable.img_emoji_clock_1100,
    R.drawable.img_emoji_clock_1130
)

internal fun calculateClosestClockFrameIndex(time: LocalTime): Int {
    val hourNormalized = time.hour % 12
    val halfHourOffset = if (time.minute >= 30) 1 else 0

    return (hourNormalized * 2) + halfHourOffset
}


@Composable
fun SpinningClockIcon(
    spinTrigger: Int, modifier: Modifier = Modifier
) {
    val initialIndex = remember { calculateClosestClockFrameIndex(LocalTime.now()) }
    val spinProgress = remember { Animatable(0f) }

    LaunchedEffect(spinTrigger) {
        if (spinTrigger > 0) {
            spinProgress.animateTo(
                targetValue = spinProgress.value + 1f, animationSpec = tween(
                    durationMillis = 1200, easing = LinearEasing
                )
            )
        }
    }

    val currentFrame =
        ((initialIndex + (spinProgress.value * 24)).toInt() % 24).let { if (it < 0) it + 24 else it }
    val rotationAngle = spinProgress.value * 360f

    Image(
        painter = painterResource(id = clockFrames[currentFrame]),
        contentDescription = "Clock",
        modifier = modifier.graphicsLayer {
            rotationZ = rotationAngle
        })
}