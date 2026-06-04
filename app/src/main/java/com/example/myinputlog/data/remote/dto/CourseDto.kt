package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class CourseDto(
    @DocumentId val id: String? = null,
    val name: String? = null,
    val goalInHours: Long? = null,
    val otherSourceHours: Long? = null,
    val timestamp: Timestamp? = null,
    val isDeleted: Boolean? = null,
    val lastUpdated: Timestamp? = null
)