package com.example.myinputlog.data.service

import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.model.ChannelWithLabelIds
import com.example.myinputlog.data.local.model.VideoWithLabelIds
import com.example.myinputlog.data.remote.dto.ChannelDto
import com.example.myinputlog.data.remote.dto.CourseDto
import com.example.myinputlog.data.remote.dto.LabelDto
import com.example.myinputlog.data.remote.dto.SyncPointersDto
import com.example.myinputlog.data.remote.dto.VideoDto
import java.util.Date

interface StorageService {
    suspend fun pushMonths(userId: String, months: Map<String, List<VideoWithLabelIds>>)
    suspend fun pushChannels(userId: String, channels: List<ChannelWithLabelIds>)
    suspend fun pushMetadata(userId: String, courses: List<CourseEntity>, labels: List<LabelEntity>)

    suspend fun getSyncPointers(userId: String): SyncPointersDto?
    suspend fun getLastUpdatedCourses(userId: String, lastPull: Date): List<CourseDto>
    suspend fun getLastUpdatedLabels(userId: String): List<LabelDto>
    suspend fun getLastUpdatedVideos(userId: String, lastPull: Date): List<VideoDto>
    suspend fun getLastUpdatedChannels(userId: String, lastPull: Date): List<ChannelDto>
}