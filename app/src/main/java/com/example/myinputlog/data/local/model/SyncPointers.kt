package com.example.myinputlog.data.local.model

data class SyncPointers(
    val labelsLastUpdated: Long = 0L,
    val channelsLastUpdated: Long = 0L,
    val coursesLastUpdated: Long = 0L
)
