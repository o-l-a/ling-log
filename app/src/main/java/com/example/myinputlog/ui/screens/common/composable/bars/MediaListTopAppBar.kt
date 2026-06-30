package com.example.myinputlog.ui.screens.common.composable.bars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.myinputlog.R
import com.example.myinputlog.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListTopAppBar(
    textFieldState: TextFieldState,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text }.collect { newText ->
            onSearch(newText.toString())
        }
    }

    TopAppBar(
        scrollBehavior = scrollBehavior, modifier = modifier, colors = colors, title = {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .weight(1f)
                        .height(MaterialTheme.spacing.largePlus)
                ) {
                    BasicTextField(
                        state = textFieldState,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search, keyboardType = KeyboardType.Text
                        ),
                        onKeyboardAction = {
                            onSearch(textFieldState.text.toString())
                            focusManager.clearFocus()
                        },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxSize(),
                        decorator = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    enabled = false, onClick = {}) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    if (textFieldState.text.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.search_text),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                                if (textFieldState.text.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            textFieldState.edit { delete(0, length) }
                                            onSearch("")
                                        },
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                        )
                                    }
                                }
                            }
                        })
                }
                FilledTonalIconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune, contentDescription = "Filters"
                    )
                }
            }
        })
}