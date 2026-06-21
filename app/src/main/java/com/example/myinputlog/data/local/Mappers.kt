package com.example.myinputlog.data.local

import com.example.myinputlog.data.local.entities.ChannelEntity
import com.example.myinputlog.data.local.entities.CountryGroupEntity
import com.example.myinputlog.data.local.entities.CourseEntity
import com.example.myinputlog.data.local.entities.LabelEntity
import com.example.myinputlog.data.local.entities.VideoEntity
import com.example.myinputlog.data.local.model.ChannelWithLabelIds
import com.example.myinputlog.data.local.model.VideoWithLabelIds
import com.example.myinputlog.data.remote.dto.ChannelDto
import com.example.myinputlog.data.remote.dto.CountryGroupDto
import com.example.myinputlog.data.remote.dto.CourseDto
import com.example.myinputlog.data.remote.dto.LabelDto
import com.example.myinputlog.data.remote.dto.VideoDto
import com.google.firebase.firestore.FieldValue
import java.util.Date

// VIDEO
fun VideoWithLabelIds.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to video.id,
    "videoId" to video.videoId,
    "courseId" to video.courseId,
    "channelId" to video.channelId,
    "watchedOn" to video.watchedOn,
    "speakersNationality" to video.speakersNationality,
    "title" to video.title,
    "durationInSeconds" to video.durationInSeconds,
    "videoUrl" to video.videoUrl,
    "thumbnailDefaultUrl" to video.thumbnailDefaultUrl,
    "thumbnailMediumUrl" to video.thumbnailDefaultUrl,
    "thumbnailHighUrl" to video.thumbnailHighUrl,
    "defaultAudioLanguage" to video.defaultAudioLanguage,
    "isDeleted" to video.isDeleted,
    "lastUpdated" to FieldValue.serverTimestamp(),
    "labelIds" to labelIds
)

fun VideoDto.toEntity(): VideoWithLabelIds = VideoWithLabelIds(
    video = VideoEntity(
        id = id ?: "",
        videoId = videoId ?: "",
        courseId = courseId ?: "",
        channelId = channelId ?: "",
        watchedOn = watchedOn?.toDate() ?: Date(),
        speakersNationality = speakersNationality,
        title = title ?: "",
        durationInSeconds = durationInSeconds ?: 0L,
        videoUrl = videoUrl ?: "",
        thumbnailDefaultUrl = thumbnailDefaultUrl ?: "",
        thumbnailMediumUrl = thumbnailMediumUrl ?: "",
        thumbnailHighUrl = thumbnailHighUrl ?: "",
        defaultAudioLanguage = defaultAudioLanguage ?: "",
        isDeleted = isDeleted ?: false,
        lastUpdated = lastUpdated?.toDate()?.time ?: 0L
    ), labelIds = labelIds ?: emptyList()
)

// CHANNEL
fun ChannelWithLabelIds.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to channel.id,
    "courseId" to channel.courseId,
    "title" to channel.title,
    "customUrl" to channel.customUrl,
    "country" to channel.country,
    "thumbnailDefaultUrl" to channel.thumbnailDefaultUrl,
    "thumbnailMediumUrl" to channel.thumbnailDefaultUrl,
    "thumbnailHighUrl" to channel.thumbnailHighUrl,
    "isDeleted" to channel.isDeleted,
    "lastUpdated" to FieldValue.serverTimestamp(),
    "labelIds" to labelIds
)

fun ChannelDto.toEntity(): ChannelWithLabelIds = ChannelWithLabelIds(
    channel = ChannelEntity(
        id = id ?: "",
        courseId = courseId ?: "",
        title = title ?: "",
        customUrl = customUrl,
        country = country,
        thumbnailDefaultUrl = thumbnailDefaultUrl ?: "",
        thumbnailMediumUrl = thumbnailMediumUrl ?: "",
        thumbnailHighUrl = thumbnailHighUrl ?: "",
        isDeleted = isDeleted ?: false,
        lastUpdated = lastUpdated?.toDate()?.time ?: 0L
    ), labelIds = labelIds ?: emptyList()
)

// LABEL
fun LabelEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "color" to color,
    "textColor" to textColor,
    "isDeleted" to isDeleted,
    "lastUpdated" to FieldValue.serverTimestamp(),
)

fun LabelDto.toEntity(): LabelEntity = LabelEntity(
    id = id ?: "",
    title = title ?: "",
    color = color ?: 0L,
    textColor = textColor ?: 0L,
    isDeleted = isDeleted ?: false,
    lastUpdated = lastUpdated?.toDate()?.time ?: 0L
)

// COURSE
fun CourseEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "goalInHours" to goalInHours,
    "otherSourceHours" to otherSourceHours,
    "timestamp" to timestamp,
    "isDeleted" to isDeleted,
    "lastUpdated" to FieldValue.serverTimestamp(),
)

fun CourseDto.toEntity(): CourseEntity = CourseEntity(
    id = id ?: "",
    name = name ?: "",
    goalInHours = goalInHours ?: 0L,
    otherSourceHours = otherSourceHours ?: 0L,
    timestamp = timestamp?.toDate() ?: Date(),
    isDeleted = isDeleted ?: false,
    lastUpdated = lastUpdated?.toDate()?.time ?: 0L
)

// APP CONFIG
fun CountryGroupDto.toEntity(): CountryGroupEntity = CountryGroupEntity(
    id = id,
    nameKey = nameKey,
    fallbackName = fallbackName,
    isoCodes = isoCodes
)