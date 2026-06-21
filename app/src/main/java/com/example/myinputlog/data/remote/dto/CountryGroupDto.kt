package com.example.myinputlog.data.remote.dto

data class CountryGroupDto(
    val id: String = "",
    val nameKey: String = "",
    val fallbackName: String = "",
    val isoCodes: List<String> = emptyList()
)
