package com.example.myinputlog.ui.screens.common.composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun ConfettiOverlay(
    modifier: Modifier,
    stopParty: () -> Unit,
    duration: Long = 5L,
    timeToLive: Long = 3500L,
    maxSpeed: Float = 15F,
    colors: List<Int> = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def)
) {
    KonfettiView(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1F), parties = listOf(
            Party(
                speed = 0f,
                maxSpeed = maxSpeed,
                damping = 0.9f,
                angle = Angle.BOTTOM,
                spread = Spread.ROUND,
                colors = colors,
                emitter = Emitter(duration = duration, TimeUnit.SECONDS).perSecond(100),
                position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0)),
                timeToLive = timeToLive
            )
        ), updateListener = object : OnParticleSystemUpdateListener {
            override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                if (activeSystems == 0) stopParty()
            }
        })
}