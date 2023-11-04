package com.example.myinputlog.ui.navigation

/**
 * The app's navigation destinations' description.
 *
 * @property route A path for a destination composable.
 * @property titleRes The destination screen's title's resource id.
 */
interface NavigationDestination {
    val route: String
    val titleRes: Int
}