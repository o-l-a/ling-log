package com.example.myinputlog.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.course.CourseDestination
import com.example.myinputlog.ui.screens.course.CourseScreen
import com.example.myinputlog.ui.screens.course.CourseViewModel
import com.example.myinputlog.ui.screens.home.HomeDestination
import com.example.myinputlog.ui.screens.home.HomeScreen
import com.example.myinputlog.ui.screens.home.HomeViewModel
import com.example.myinputlog.ui.screens.landing.LandingDestination
import com.example.myinputlog.ui.screens.landing.LandingScreen
import com.example.myinputlog.ui.screens.landing.LandingViewModel
import com.example.myinputlog.ui.screens.login.LoginDestination
import com.example.myinputlog.ui.screens.login.LoginScreen
import com.example.myinputlog.ui.screens.login.LoginViewModel
import com.example.myinputlog.ui.screens.profile.ProfileDestination
import com.example.myinputlog.ui.screens.profile.ProfileScreen
import com.example.myinputlog.ui.screens.profile.ProfileViewModel
import com.example.myinputlog.ui.screens.sign_up.SignUpDestination
import com.example.myinputlog.ui.screens.sign_up.SignUpScreen
import com.example.myinputlog.ui.screens.sign_up.SignUpViewModel
import com.example.myinputlog.ui.screens.video.VideoDestination
import com.example.myinputlog.ui.screens.video.VideoScreen
import com.example.myinputlog.ui.screens.video.VideoViewModel
import com.example.myinputlog.ui.screens.video_list.VideoListDestination
import com.example.myinputlog.ui.screens.video_list.VideoListScreen
import com.example.myinputlog.ui.screens.video_list.VideoListViewModel

const val DEFAULT_ID = -1
const val HOME_ROUTE = "home_route"
const val VIDEOS_ROUTE = "videos_route"
const val PROFILE_ROUTE = "profile_route"
const val SIGN_IN_ROUTE = "sign_in_route"

sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int?,
    val icon: ImageVector
) {
    object Home : Screen(HOME_ROUTE, R.string.home_bottom_nav_description, Icons.Filled.Home)
    object Videos : Screen(VIDEOS_ROUTE, R.string.videos_bottom_nav_description, Icons.Filled.VideoLibrary)
    object AddVideo : Screen("", null, Icons.Outlined.AddCircleOutline)
    object Recommendations : Screen("", R.string.suggested_bottom_nav_description, Icons.Filled.SmartDisplay)
    object Profile : Screen(PROFILE_ROUTE, R.string.profile_bottom_nav_description, Icons.Filled.Person)
}

val navigationItems = listOf(
    Screen.Home,
    Screen.Videos,
    Screen.AddVideo,
    Screen.Recommendations,
    Screen.Profile,
)

@Composable
fun MyInputLogNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = LandingDestination.route
    ) {
        composable(
            route = LandingDestination.route
        ) {
            val landingViewModel = hiltViewModel<LandingViewModel>()
            LandingScreen(
                navigateWithPopUp = { route ->
                    navController.navigateWithPopUp(route, LandingDestination.route)
                },
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
    navigation(
        startDestination = HomeDestination.route,
        route = HOME_ROUTE
    ) {
        composable(
            route = HomeDestination.route
        ) {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(
                homeViewModel = homeViewModel,
                onBottomNavClicked = { route ->
                    navController.navigate(route)
                },
                navigateToYouTubeVideoEntry = { courseId ->
                    navController.navigate("${VideoDestination.route}/$courseId/$DEFAULT_ID")
                },
                navigateToYouTubeVideo = { courseId, videoId ->
                    navController.navigate("${VideoDestination.route}/$courseId/$videoId")
                }
            )
        }
    }
}

fun NavGraphBuilder.myInputLogVideosGraph(navController: NavHostController) {
    navigation(
        startDestination = VideoListDestination.route,
        route = VIDEOS_ROUTE
    ) {
        composable(
            route = VideoListDestination.route
        ) {
            val videoListViewModel = hiltViewModel<VideoListViewModel>()
            VideoListScreen(
                videoListViewModel = videoListViewModel,
                onBottomNavClicked = { route ->
                    navController.navigateWithPopUp(route, VideoListDestination.route)
                },
                navigateToYouTubeVideoEntry = { courseId ->
                    navController.navigate("${VideoDestination.route}/$courseId/$DEFAULT_ID")
                },
                navigateToYouTubeVideo = { courseId, videoId ->
                    navController.navigate("${VideoDestination.route}/$courseId/$videoId")
                }
            )
        }
        composable(
            route = VideoDestination.routeWithArgs,
            arguments = listOf(
                navArgument(VideoDestination.videoIdArg) { type = NavType.StringType },
                navArgument(VideoDestination.courseIdArg) { type = NavType.StringType }
            )
        ) {
            val videoViewModel = hiltViewModel<VideoViewModel>()
            VideoScreen(
                videoViewModel = videoViewModel,
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}

fun NavGraphBuilder.myInputLogProfileGraph(navController: NavHostController) {
    navigation(
        startDestination = ProfileDestination.route,
        route = PROFILE_ROUTE
    ) {
        composable(
            route = ProfileDestination.route
        ) {
            val profileViewModel = hiltViewModel<ProfileViewModel>()
            ProfileScreen(
                profileViewModel = profileViewModel,
                onBottomNavClicked = { route ->
                    navController.navigateWithPopUp(route, ProfileDestination.route)
                },
                navigateToUserCourseEntry = { navController.navigate("${CourseDestination.route}/$DEFAULT_ID") },
                navigateToUserCourse = { courseId ->
                    navController.navigate("${CourseDestination.route}/$courseId")
                },
                navigateWithPopUp = {
                    navController.navigateWithPopUp(
                        LoginDestination.route, HomeDestination.route
                    )
                },
                navigateToYouTubeVideoEntry = { courseId ->
                    navController.navigate("${VideoDestination.route}/$courseId/$DEFAULT_ID")
                }
            )
        }
        composable(
            route = CourseDestination.routeWithArgs,
            arguments = listOf(navArgument(CourseDestination.courseIdArg) {
                type = NavType.StringType
            })
        ) {
            val courseViewModel = hiltViewModel<CourseViewModel>()
            CourseScreen(
                courseViewModel = courseViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}

fun NavGraphBuilder.myInputLogSignInGraph(navController: NavHostController) {
    navigation(
        startDestination = LoginDestination.route,
        route = SIGN_IN_ROUTE
    ) {
        composable(
            route = LoginDestination.route
        ) {
            val loginViewModel = hiltViewModel<LoginViewModel>()
            LoginScreen(
                viewModel = loginViewModel,
                onSignUpClick = {
                    navController.navigateWithPopUp(
                        SignUpDestination.route, LoginDestination.route
                    )
                },
                onLoginClick = {
                    navController.navigateWithPopUp(
                        HomeDestination.route, LoginDestination.route
                    )
                }
            )
        }
        composable(
            route = SignUpDestination.route
        ) {
            val signUpViewModel = hiltViewModel<SignUpViewModel>()
            SignUpScreen(
                viewModel = signUpViewModel,
                onSignInClick = {
                    navController.navigate(LoginDestination.route)
                },
                onSignUpClick = {
                    navController.navigateWithPopUp(
                        HomeDestination.route, SignUpDestination.route
                    )
                }
            )
        }
    }
}

fun NavHostController.navigateWithPopUp(route: String, popUp: String) {
    navigate(route) {
        popUpTo(popUp) { inclusive = true }
        launchSingleTop = true
    }
}

