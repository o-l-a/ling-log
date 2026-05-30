package com.example.myinputlog.ui.screens.ui_settings

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.MyInputLogTopAppBar
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.screens.utils.composable.ColorSwatch
import com.example.myinputlog.ui.screens.utils.composable.ConfettiOverlay
import com.example.myinputlog.ui.screens.utils.composable.EmptyCollectionBox
import com.example.myinputlog.ui.screens.utils.composable.LoadingBox
import com.example.myinputlog.ui.theme.AppTheme
import com.example.myinputlog.ui.theme.spacing

private data class ThemeOption(
    val theme: AppTheme,
    @get:StringRes val labelRes: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)

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
                if (currentState.isParty) {
                    val confettiIntColors = remember(currentState.selectedConfettiVariant.colors) {
                        currentState.selectedConfettiVariant.colors.map { it.toInt() }
                    }

                    ConfettiOverlay(
                        modifier = modifier.fillMaxSize(),
                        stopParty = uiSettingsViewModel::confettiStop,
                        duration = 1L,
                        maxSpeed = 30F,
                        colors = confettiIntColors
                    )
                }
            }
        }
    }
}

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
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium + MaterialTheme.spacing.extraExtraSmall),
        state = scrollState,
        userScrollEnabled = isScrollEnabled
    ) {
        item {
            AppThemeSection(
                selectedMode = uiSettingsUiState.selectedMode, onAppThemeChange = onAppThemeChange
            )
        }

        item {
            ConfettiSection(
                value = uiSettingsUiState.selectedConfettiVariant,
                onConfettiColorsChange = onConfettiColorsChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppThemeSection(
    modifier: Modifier = Modifier,
    selectedMode: AppTheme,
    onAppThemeChange: (AppTheme) -> Unit,
) {
    val themeOptions = remember {
        listOf(
            ThemeOption(
                AppTheme.LIGHT,
                R.string.theme_light,
                Icons.Outlined.LightMode,
                Icons.Filled.LightMode
            ), ThemeOption(
                AppTheme.DARK, R.string.theme_dark, Icons.Outlined.DarkMode, Icons.Filled.DarkMode
            ), ThemeOption(
                AppTheme.SYSTEM,
                R.string.theme_system,
                Icons.Outlined.SettingsSuggest,
                Icons.Filled.SettingsSuggest
            )
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Text(
            text = stringResource(R.string.settings_app_theme),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        ) {
            themeOptions.forEachIndexed { index, option ->
                val isSelected = selectedMode == option.theme

                ToggleButton(
                    modifier = Modifier.weight(1f),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfettiSection(
    modifier: Modifier = Modifier,
    value: ConfettiOptions,
    onConfettiColorsChange: (ConfettiOptions) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Text(
            text = stringResource(R.string.settings_confetti_variant),
        )
        ExposedDropdownMenuBox(
            expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                value = stringResource(value.optionName),
                onValueChange = {},
                suffix = {
                    ColorSwatchRow(
                        colors = value.colors,
                        modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false }) {
                ConfettiOptions.entries.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stringResource(selectionOption.optionName))
                                ColorSwatchRow(colors = selectionOption.colors)
                            }

                        }, onClick = {
                            onConfettiColorsChange(selectionOption)
                            expanded = false
                        }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun ColorSwatchRow(
    colors: List<Long>, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { colorInt ->
            ColorSwatch(colorInt)
        }
    }
}