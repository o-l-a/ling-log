package com.example.myinputlog.ui.screens.common.ext

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtractYouTubeVideoIdTest {
    @Test
    fun `verify extraction for path-based and query-based watch formats`() {
        val expectedId = "dQw4w9WgXcQ"
        val testUrls = listOf(
            "https://www.youtube.com/watch/$expectedId",
            "https://www.youtube.com/watch?v=$expectedId",
            "https://m.youtube.com/watch?feature=share&v=$expectedId",
            "https://youtu.be/$expectedId?si=AbCdEf123",
            "https://youtube.com/shorts/$expectedId?si=AbCdEf123&feature=share"
        )

        testUrls.forEach { url ->
            assertEquals("Failed for URL: $url", expectedId, url.extractYouTubeVideoId())
        }
    }
}