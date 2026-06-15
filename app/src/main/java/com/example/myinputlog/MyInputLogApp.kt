package com.example.myinputlog

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myinputlog.ui.navigation.MyInputLogNavHost

/**
 * A top level screen "container"
 */
@Composable
fun MyInputLogApp(navController: NavHostController = rememberNavController()) {
    MyInputLogNavHost(navController = navController)
}
