package com.example.myinputlog.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.course.CourseScreen
import com.example.myinputlog.ui.screens.course.CourseViewModel
import com.example.myinputlog.ui.screens.home.HomeScreen
import com.example.myinputlog.ui.screens.home.HomeViewModel
import com.example.myinputlog.ui.screens.landing.LandingScreen
import com.example.myinputlog.ui.screens.landing.LandingViewModel
import com.example.myinputlog.ui.screens.login.LoginScreen
import com.example.myinputlog.ui.screens.login.LoginViewModel
import com.example.myinputlog.ui.screens.profile.ProfileScreen
import com.example.myinputlog.ui.screens.profile.ProfileViewModel
import com.example.myinputlog.ui.screens.sign_up.SignUpScreen
import com.example.myinputlog.ui.screens.sign_up.SignUpViewModel
import com.example.myinputlog.ui.screens.video.VideoScreen
import com.example.myinputlog.ui.screens.video.VideoViewModel
import com.example.myinputlog.ui.screens.video_list.VideoListScreen
import com.example.myinputlog.ui.screens.video_list.VideoListViewModel
import kotlinx.serialization.Serializable

const val DEFAULT_ID = -1

@Serializable
object HomeGraph

@Serializable
object VideosGraph

@Serializable
object AuthGraph

@Serializable
object ProfileGraph

@Serializable
object LandingRoute

@Serializable
object HomeRoute

@Serializable
object VideoListRoute

@Serializable
data class VideoRoute(val courseId: String, val videoId: String, val videoUrl: String? = null)

@Serializable
object PlaylistsRoute

@Serializable
object ProfileRoute

@Serializable
data class CourseRoute(val courseId: String)

@Serializable
object LoginRoute

@Serializable
object SignUpRoute

sealed class Screen(
    val route: Any, @get:StringRes val resourceId: Int?, val icon: ImageVector
) {
    object Home : Screen(HomeRoute, R.string.home_bottom_nav_description, Icons.Filled.Home)
    object Videos :
        Screen(VideoListRoute, R.string.videos_bottom_nav_description, Icons.Filled.VideoLibrary)

    object AddVideo : Screen("", null, Icons.Outlined.AddCircleOutline)
    object RecentlyWatched : Screen(
        PlaylistsRoute,
        R.string.suggested_bottom_nav_description,
        Icons.AutoMirrored.Filled.PlaylistPlay
    )

    object Profile :
        Screen(ProfileRoute, R.string.profile_bottom_nav_description, Icons.Filled.Person)
}

val navigationItems = listOf(
    Screen.Home,
    Screen.Videos,
    Screen.AddVideo,
    Screen.RecentlyWatched,
    Screen.Profile,
)

@Composable
fun MyInputLogNavHost(
    modifier: Modifier = Modifier, navController: NavHostController
) {
    NavHost(
        modifier = modifier, navController = navController, startDestination = LandingRoute
    ) {
        composable<LandingRoute> {
            val landingViewModel = hiltViewModel<LandingViewModel>()
            LandingScreen(
                navigateWithPopUp = { navController.navigateWithPopUp(HomeGraph, LandingRoute) },
                viewModel = landingViewModel
            )
        }
        myInputLogHomeGraph(navController)
        myInputLogSignInGraph(navController)
        myInputLogVideosGraph(navController)
        myInputLogProfileGraph(navController)
    }
}

fun NavGraphBuilder.myInputLogHomeGraph(navController: NavHostController) {
    navigation<HomeGraph>(
        startDestination = HomeRoute,
    ) {
        composable<HomeRoute> {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(homeViewModel = homeViewModel, onBottomNavClicked = { route ->
                navController.navigate(route)
            }, navigateToYouTubeVideoEntry = { courseId ->
                navController.navigate(VideoRoute(courseId, DEFAULT_ID.toString()))
            })
        }
    }
}

fun NavGraphBuilder.myInputLogVideosGraph(navController: NavHostController) {
    navigation<VideosGraph>(
        startDestination = VideoListRoute,
    ) {
        composable<VideoListRoute> {
            val videoListViewModel = hiltViewModel<VideoListViewModel>()
            VideoListScreen(videoListViewModel = videoListViewModel, onBottomNavClicked = { route ->
                navController.navigateWithPopUp(route, VideoListRoute)
            }, navigateToYouTubeVideoEntry = { courseId ->
                navController.navigate(VideoRoute(courseId, DEFAULT_ID.toString()))
            }, navigateToYouTubeVideo = { courseId, videoId ->
                navController.navigate(VideoRoute(courseId, videoId))
            })
        }
        composable<VideoRoute> {
            val videoViewModel = hiltViewModel<VideoViewModel>()
            VideoScreen(
                videoViewModel = videoViewModel,
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() })
        }
    }
}

fun NavGraphBuilder.myInputLogProfileGraph(navController: NavHostController) {
    navigation<ProfileGraph>(
        startDestination = ProfileRoute,
    ) {
        composable<ProfileRoute> {
            val profileViewModel = hiltViewModel<ProfileViewModel>()
            ProfileScreen(
                profileViewModel = profileViewModel,
                onBottomNavClicked = { route ->
                    navController.navigateWithPopUp(route, ProfileRoute)
                },
                navigateToUserCourseEntry = { navController.navigate(CourseRoute(DEFAULT_ID.toString())) },
                navigateToUserCourse = { courseId ->
                    navController.navigate(CourseRoute(courseId))
                },
                navigateWithPopUp = {
                    navController.navigateWithPopUp(
                        LoginRoute, HomeRoute
                    )
                },
                navigateToYouTubeVideoEntry = { courseId ->
                    navController.navigate(VideoRoute(courseId, DEFAULT_ID.toString()))
                })
        }
        composable<CourseRoute> {
            val courseViewModel = hiltViewModel<CourseViewModel>()
            CourseScreen(
                courseViewModel = courseViewModel, onNavigateUp = { navController.navigateUp() })
        }
    }
}

fun NavGraphBuilder.myInputLogSignInGraph(navController: NavHostController) {
    navigation<AuthGraph>(
        startDestination = LoginRoute
    ) {
        composable<LoginRoute> {
            val loginViewModel = hiltViewModel<LoginViewModel>()
            LoginScreen(viewModel = loginViewModel, onSignUpClick = {
                navController.navigateWithPopUp(
                    SignUpRoute, LoginRoute
                )
            }, onLoginClick = {
                navController.navigateWithPopUp(
                    HomeRoute, LoginRoute
                )
            })
        }
        composable<SignUpRoute> {
            val signUpViewModel = hiltViewModel<SignUpViewModel>()
            SignUpScreen(viewModel = signUpViewModel, onSignInClick = {
                navController.navigate(LoginRoute)
            }, onSignUpClick = {
                navController.navigateWithPopUp(
                    HomeRoute, SignUpRoute
                )
            })
        }
    }
}

fun NavHostController.navigateWithPopUp(route: Any, popUpToRoute: Any) {
    navigate(route) {
        popUpTo(popUpToRoute) { inclusive = true }
        launchSingleTop = true
    }
}
