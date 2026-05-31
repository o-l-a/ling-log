package com.example.myinputlog.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material.icons.filled.VideoLibrary
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
import com.example.myinputlog.ui.screens.account.AccountScreen
import com.example.myinputlog.ui.screens.account.AccountViewModel
import com.example.myinputlog.ui.screens.channel.ChannelScreen
import com.example.myinputlog.ui.screens.channel.ChannelViewModel
import com.example.myinputlog.ui.screens.course.CourseScreen
import com.example.myinputlog.ui.screens.course.CourseViewModel
import com.example.myinputlog.ui.screens.course_list.CourseListScreen
import com.example.myinputlog.ui.screens.course_list.CourseListViewModel
import com.example.myinputlog.ui.screens.home.HomeScreen
import com.example.myinputlog.ui.screens.home.HomeViewModel
import com.example.myinputlog.ui.screens.landing.LandingScreen
import com.example.myinputlog.ui.screens.landing.LandingViewModel
import com.example.myinputlog.ui.screens.login.LoginScreen
import com.example.myinputlog.ui.screens.login.LoginViewModel
import com.example.myinputlog.ui.screens.media_list.MediaListScreen
import com.example.myinputlog.ui.screens.media_list.MediaListViewModel
import com.example.myinputlog.ui.screens.profile.ProfileScreen
import com.example.myinputlog.ui.screens.profile.ProfileViewModel
import com.example.myinputlog.ui.screens.sign_up.SignUpScreen
import com.example.myinputlog.ui.screens.sign_up.SignUpViewModel
import com.example.myinputlog.ui.screens.ui_settings.UiSettingsScreen
import com.example.myinputlog.ui.screens.ui_settings.UiSettingsViewModel
import com.example.myinputlog.ui.screens.video.VideoScreen
import com.example.myinputlog.ui.screens.video.VideoViewModel

const val DEFAULT_ID = -1

sealed class Screen(
    val route: Any, @get:StringRes val resourceId: Int?, val icon: ImageVector
) {
    object Home : Screen(HomeRoute, R.string.home_bottom_nav_description, Icons.Filled.Home)
    object Videos :
        Screen(MediaListRoute, R.string.media_bottom_nav_description, Icons.Filled.VideoLibrary)

    object AddVideo : Screen("", null, Icons.Filled.Add)
    object RecentlyWatched : Screen(
        PlaylistsRoute, R.string.suggested_bottom_nav_description, Icons.Filled.StackedBarChart
    )

    object Profile :
        Screen(ProfileRoute, R.string.profile_bottom_nav_description, Icons.Filled.Person)
}

sealed class SettingsScreen(
    val route: Any, @get:StringRes val resourceId: Int
) {
    object Account : SettingsScreen(AccountRoute, R.string.account_nav_description)
    object Labels : SettingsScreen(LabelListRoute, R.string.label_list_nav_description)
    object UiSettings : SettingsScreen(UiSettingsRoute, R.string.ui_settings_nav_description)
    object Courses : SettingsScreen(CourseListRoute, R.string.course_list_nav_description)
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
                navigateWithPopUp = { route ->
                    navController.navigateWithPopUp(
                        route, LandingRoute
                    )
                }, viewModel = landingViewModel
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
    navigation<MediaGraph>(
        startDestination = MediaListRoute,
    ) {
        composable<MediaListRoute> {
            val mediaListViewModel = hiltViewModel<MediaListViewModel>()
            MediaListScreen(mediaListViewModel = mediaListViewModel, onBottomNavClicked = { route ->
                navController.navigateWithPopUp(route, MediaListRoute)
            }, navigateToYouTubeVideoEntry = { courseId ->
                navController.navigate(VideoRoute(courseId, DEFAULT_ID.toString()))
            }, navigateToYouTubeVideo = { courseId, videoId ->
                navController.navigate(VideoRoute(courseId, videoId))
            }, navigateToYouTubeChannel = { courseId, channelId ->
                navController.navigate(ChannelRoute(courseId, channelId))
            })
        }
        composable<VideoRoute> {
            val videoViewModel = hiltViewModel<VideoViewModel>()
            VideoScreen(
                videoViewModel = videoViewModel,
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() })
        }
        composable<ChannelRoute> {
            val channelViewModel = hiltViewModel<ChannelViewModel>()
            ChannelScreen(
                channelViewModel = channelViewModel,
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
                navigationItems = mapOf(
                    SettingsScreen.Courses to { navController.navigate(CourseListRoute) },
                    SettingsScreen.Labels to { navController.navigate(LabelListRoute) },
                    SettingsScreen.UiSettings to { navController.navigate(UiSettingsRoute) },
                    SettingsScreen.Account to { navController.navigate(AccountRoute) }),
                navigateToYouTubeVideoEntry = { courseId ->
                    navController.navigate(VideoRoute(courseId, DEFAULT_ID.toString()))
                })
        }
        composable<CourseListRoute> {
            val courseListViewModel = hiltViewModel<CourseListViewModel>()
            CourseListScreen(
                courseListViewModel = courseListViewModel,
                onNavigateUp = { navController.navigateUp() },
                navigateToUserCourseEntry = { navController.navigate(CourseRoute(DEFAULT_ID.toString())) },
                navigateToUserCourse = { courseId ->
                    navController.navigate(CourseRoute(courseId))
                })
        }
        composable<CourseRoute> {
            val courseViewModel = hiltViewModel<CourseViewModel>()
            CourseScreen(
                courseViewModel = courseViewModel, onNavigateUp = { navController.navigateUp() })
        }
        composable<UiSettingsRoute> {
            val uiSettingsViewModel = hiltViewModel<UiSettingsViewModel>()
            UiSettingsScreen(
                uiSettingsViewModel = uiSettingsViewModel,
                onNavigateUp = { navController.navigateUp() })
        }
        composable<AccountRoute> {
            val accountViewModel = hiltViewModel<AccountViewModel>()
            AccountScreen(
                accountViewModel = accountViewModel,
                onNavigateUp = { navController.navigateUp() },
                navigateWithPopUp = {
                    navController.navigate(LoginRoute) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                })
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
