package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class CourseDto(
    val id: String? = null,
    val name: String? = null,
    val goalInHours: Long? = null,
    val otherSourceHours: Long? = null,
    val timestamp: Timestamp? = null,
    @get:PropertyName("isDeleted")
    val isDeleted: Boolean? = null,
    val lastUpdated: Timestamp? = null
)