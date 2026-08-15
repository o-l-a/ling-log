package com.example.myinputlog.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
/**
 * Just the home page
 */
object HomeGraph

@Serializable
/**
 * All media-related operations
 */
object MediaGraph

@Serializable
/**
 * Trends page
 */
object TrendsGraph

@Serializable
/**
 * Authentication flow (log in, create account)
 */
object AuthGraph

@Serializable
/**
 * User profile and settings operations
 */
object ProfileGraph

@Serializable
/**
 * Landing page with just the app icon
 */
object LandingRoute

@Serializable
/**
 * Home page with calendar and basic statistics
 */
object HomeRoute

@Serializable
/**
 * Media page with video list and channel list
 */
data class MediaListRoute(
    val targetDate: Long? = null
)

@Serializable
/**
 * Video CRUD page
 */
data class VideoRoute(val courseId: String, val videoId: String, val videoUrl: String? = null)

@Serializable
/**
 * Channel CRUD page
 */
data class ChannelRoute(val courseId: String, val channelId: String)

@Serializable
/**
 * To yeet
 */
object TrendsRoute

@Serializable
/**
 * Profile page with settings sections
 */
object ProfileRoute

@Serializable
/**
 * Page with course list
 */
object CourseListRoute

@Serializable
/**
 * Course CRUD page
 */
data class CourseRoute(val courseId: String)

@Serializable
/**
 * Page with label list
 */
object LabelListRoute

@Serializable
/**
 * Label CRUD page
 */
data class LabelRoute(val labelId: String)

@Serializable
/**
 * Page with account settings
 */
object AccountRoute

@Serializable
/**
 * Page with UI settings
 */
object UiSettingsRoute

@Serializable
/**
 * Login page
 */
object LoginRoute

@Serializable
/**
 * Sign up page
 */
object SignUpRoute