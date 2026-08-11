package com.example.myinputlog.data.remote.dto

import com.example.myinputlog.data.local.model.SyncPointers
import com.google.firebase.Timestamp

data class SyncPointersDto(
    val labelsLastUpdated: Timestamp? = null,
    val channelsLastUpdated: Timestamp? = null,
    val coursesLastUpdated: Timestamp? = null
) {
    fun toDomain() = SyncPointers(
        labelsLastUpdated = labelsLastUpdated?.toDate()?.time ?: 0L,
        channelsLastUpdated = channelsLastUpdated?.toDate()?.time ?: 0L,
        coursesLastUpdated = coursesLastUpdated?.toDate()?.time ?: 0L
    )
}