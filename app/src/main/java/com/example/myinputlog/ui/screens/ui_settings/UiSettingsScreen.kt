package com.example.myinputlog.ui.screens.ui_settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.theme.AppTheme
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiSettingsScreen(
    modifier: Modifier = Modifier,
    uiSettingsViewModel: UiSettingsViewModel,
    onNavigateUp: () -> Unit
) {
    val uiSettingsUiState by uiSettingsViewModel.uiSettingsUiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        MyInputLogTopAppBar(
            title = "",
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            scrollBehavior = scrollBehavior
        )
    }) { innerPadding ->
        when (val currentState = uiSettingsUiState) {
            UiSettingsUiState.Error -> {
                EmptyCollectionBox(
                    modifier = modifier.padding(MaterialTheme.spacing.medium),
                    bodyMessage = R.string.something_went_wrong
                )
            }

            UiSettingsUiState.Loading -> {
                LoadingBox()
            }

            is UiSettingsUiState.Success -> {
                UiSettingsBody(
                    modifier = Modifier.padding(innerPadding),
                    uiSettingsUiState = currentState,
                    onAppThemeChange = uiSettingsViewModel::setTheme,
                    onConfettiColorsChange = uiSettingsViewModel::setConfetti
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UiSettingsBody(
    modifier: Modifier = Modifier,
    uiSettingsUiState: UiSettingsUiState.Success,
    onAppThemeChange: (AppTheme) -> Unit,
    onConfettiColorsChange: (ConfettiOptions) -> Unit
) {
    val scrollState = rememberLazyListState()
    val isScrollEnabled by remember {
        derivedStateOf { scrollState.canScrollForward || scrollState.canScrollBackward }
    }
    val themeOptions = remember {
        listOf(
            ThemeOption(
                AppTheme.LIGHT,
                R.string.theme_light,
                Icons.Outlined.LightMode,
                Icons.Filled.LightMode
            ),
            ThemeOption(
                AppTheme.DARK,
                R.string.theme_dark,
                Icons.Outlined.DarkMode,
                Icons.Filled.DarkMode
            ),
            ThemeOption(
                AppTheme.SYSTEM,
                R.string.theme_system,
                Icons.Outlined.SettingsSuggest,
                Icons.Filled.SettingsSuggest
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall),
        state = scrollState,
        userScrollEnabled = isScrollEnabled
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                ) {
                    themeOptions.forEachIndexed { index, option ->
                        val isSelected = uiSettingsUiState.selectedMode == option.theme

                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = { if (it) onAppThemeChange(option.theme) },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                themeOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Icon(
                                imageVector = if (isSelected) option.selectedIcon else option.unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                            Text(text = stringResource(option.labelRes))
                        }
                    }
                }
            }
        }
    }
}

private data class ThemeOption(
    val theme: AppTheme,
    @get:StringRes val labelRes: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)