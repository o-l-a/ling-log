package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp

data class VideoDtoWrapper(
    val videos: Map<String, VideoDto>? = null, val lastUpdated: Timestamp? = null
)