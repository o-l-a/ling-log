package com.example.myinputlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myinputlog.ui.navigation.MyInputLogNavHost
import com.example.myinputlog.ui.navigation.Screen
import com.example.myinputlog.ui.navigation.navigationItems

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
    hasEditAction: Boolean = false,
    hasDeleteAction: Boolean = false,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
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
                if (hasEditAction) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_text))
                    }
                }
                if (hasDeleteAction) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_text))
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
) {
    NavigationBar(
        modifier = modifier
    ) {
        navigationItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.resourceId)) },
                selected = selectedScreen.route == screen.route,
                onClick = { if (selectedScreen.route != screen.route) onBottomNavClicked(screen.route) }
            )
        }
    }
}

/**
 * App bottom bar for screens involving edit actions
 */
@Composable
fun MyInputLogBottomSaveBar(
    modifier: Modifier = Modifier,
    isFormValid: Boolean = false,
    onCancelClicked: () -> Unit,
    onSaveClicked: () -> Unit
) {
    BottomAppBar(
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onCancelClicked) {
                Text(stringResource(R.string.cancel_text))
            }
            TextButton(
                onClick = onSaveClicked,
                enabled = isFormValid
            ) {
                Text(stringResource(R.string.save_text))
            }
        }
    }
}