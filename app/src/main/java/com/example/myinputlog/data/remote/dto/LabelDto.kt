package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class LabelDto(
    val id: String? = null,
    val title: String? = null,
    val color: Long? = null,
    val textColor: Long? = null,
    @get:PropertyName("isDeleted")
    val isDeleted: Boolean? = null,
    val lastUpdated: Timestamp? = null
)