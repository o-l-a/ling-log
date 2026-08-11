package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp

data class LabelDtoWrapper(
    val labels: Map<String, LabelDto>? = null, val lastUpdated: Timestamp? = null
)