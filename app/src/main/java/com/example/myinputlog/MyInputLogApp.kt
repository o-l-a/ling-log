package com.example.myinputlog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.ui.navigation.MyInputLogNavHost
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.navigation.navigationItems
import com.example.myinputlog.ui.screens.utils.composable.MyInputLogAppIcon
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing
import java.util.concurrent.TimeUnit

/**
 * A top level screen "container"
 */
@Composable
fun MyInputLogApp(navController: NavHostController = rememberNavController()) {
    MyInputLogNavHost(navController = navController)
}

/**
 * App top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInputLogTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    canNavigateBack: Boolean,
    hasSaveAction: Boolean = false,
    hasDeleteAction: Boolean = false,
    isFormValid: Boolean = false,
    onDelete: () -> Unit = {},
    onSave: () -> Unit = {},
    navigateUp: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    if (canNavigateBack) {
        TopAppBar(
            modifier = modifier,
            title = {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button_content_description)
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            actions = {
                if (hasDeleteAction) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_text)
                        )
                    }
                }
                if (hasSaveAction) {
                    IconButton(onClick = onSave, enabled = isFormValid) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = stringResource(R.string.save_text)
                        )
                    }
                }
            }
        )
    } else {
        TopAppBar(
            modifier = modifier,
            title = { Text(title) }
        )
    }
}

/**
 * App bottom bar for primary screens
 */
@Composable
fun MyInputLogBottomNavBar(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onBottomNavClicked: (String) -> Unit,
    navigateToYouTubeVideoEntry: () -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets(
            left = MaterialTheme.spacing.extraSmall,
            right = MaterialTheme.spacing.extraSmall,
        )
    ) {
        navigationItems.forEach { screen ->
            if (screen is Screen.AddVideo) {
                Box(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.default)
                        .width(MaterialTheme.spacing.extraLarge)
                ) {
                    Row {
                        NavigationBarItem(
                            onClick = navigateToYouTubeVideoEntry,
                            icon = {
                                Icon(
                                    modifier = Modifier
                                        .padding(MaterialTheme.spacing.default)
                                        .size(MaterialTheme.spacing.large),
                                    imageVector = screen.icon,
                                    contentDescription = null
                                )
                            },
                            selected = false
                        )
                    }
                }
            } else {
                NavigationBarItem(
                    modifier = Modifier.padding(MaterialTheme.spacing.default),
                    icon = {
                        Icon(
                            modifier = Modifier.padding(MaterialTheme.spacing.default),
                            imageVector = screen.icon,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            modifier = Modifier.padding(MaterialTheme.spacing.default),
                            text = stringResource(screen.resourceId!!),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    selected = selectedScreen.route == screen.route,
                    onClick = { if (selectedScreen.route != screen.route) onBottomNavClicked(screen.route) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseTopAppBar(
    modifier: Modifier = Modifier,
    course: UserCourse,
    courseStatistics: CourseStatistics,
    onValueChange: (UserCourse) -> Unit,
    options: List<UserCourse>,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val hoursWatched = TimeUnit.SECONDS.toHours(
        courseStatistics.timeWatched
    )
    val totalHours = hoursWatched + course.otherSourceHours
    val progress =
        if (course.goalInHours != 0L) totalHours.toFloat() * 100F / course.goalInHours.toFloat() else 0
    TopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            MyInputLogAppIcon(
                modifier = Modifier
                    .padding(start = MaterialTheme.spacing.medium)
                    .size(MaterialTheme.spacing.appLogoSize)
            )
        },
        title = {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(
                            R.string.progress,
                            "${progress.toLong()}% (${totalHours}h/${course.goalInHours}h)"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        },
        actions = {
            Box(
                modifier = Modifier.wrapContentSize(Alignment.TopStart)
            ) {
                IconButton(onClick = { expanded = true }) {
                    if (expanded) {
                        Icon(imageVector = Icons.Filled.ArrowDropUp, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = expanded, onDismissRequest = {
                    expanded = false
                }) {
                    options.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.name) },
                            onClick = {
                                onValueChange(selectionOption)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        })
}

@Preview
@Composable
fun BottomNavBarPreview() {
    MyInputLogTheme {
        Scaffold(
            bottomBar = {
                MyInputLogBottomNavBar(
                    selectedScreen = Screen.Home,
                    onBottomNavClicked = {},
                    navigateToYouTubeVideoEntry = {}
                )
            }
        ) {
            Text(modifier = Modifier.padding(paddingValues = it), text = "Hey")
        }
    }
}