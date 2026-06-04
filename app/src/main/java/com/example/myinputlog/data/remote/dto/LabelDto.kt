package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp

data class LabelDto(
    val id: String? = null,
    val title: String? = null,
    val color: Long? = null,
    val textColor: Long? = null,
    val isDeleted: Boolean? = null,
    val lastUpdated: Timestamp? = null
)