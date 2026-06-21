package com.example.myinputlog.data.remote.dto

import com.google.firebase.Timestamp

data class AppConfigDto(
    val lastUpdated: Timestamp? = null,
    val countryGroups: Map<String, CountryGroupDto> = emptyMap()
)