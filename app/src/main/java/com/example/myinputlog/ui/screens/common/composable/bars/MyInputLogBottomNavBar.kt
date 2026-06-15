package com.example.myinputlog.ui.screens.common.composable.bars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.myinputlog.ui.models.CourseHeaderUiModel
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.navigation.navigationItems
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing

/**
 * App bottom bar for primary screens
 */
@Composable
fun MyInputLogBottomNavBar(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onBottomNavClicked: (Any) -> Unit,
    navigateToYouTubeVideoEntry: () -> Unit,
) {
    NavigationBar(
        modifier = modifier, windowInsets = WindowInsets(
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
                            onClick = navigateToYouTubeVideoEntry, icon = {
                                Icon(
                                    modifier = Modifier
                                        .padding(MaterialTheme.spacing.default)
                                        .size(MaterialTheme.spacing.large),
                                    imageVector = screen.icon,
                                    contentDescription = null
                                )
                            }, selected = false
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
                    onClick = { if (selectedScreen.route != screen.route) onBottomNavClicked(screen.route) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BottomNavBarPreview() {
    MyInputLogTheme {
        Scaffold(topBar = {
            CourseTopAppBar(
                courseHeader = CourseHeaderUiModel(name = "Test 123"),
                onValueChange = {},
                options = emptyList(),
            )
        }, bottomBar = {
            MyInputLogBottomNavBar(
                selectedScreen = Screen.Home,
                onBottomNavClicked = {},
                navigateToYouTubeVideoEntry = {})
        }) {
            Text(modifier = Modifier.padding(paddingValues = it), text = "Hey")
        }
    }
}