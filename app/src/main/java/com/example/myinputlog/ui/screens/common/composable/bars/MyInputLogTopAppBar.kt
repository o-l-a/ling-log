package com.example.myinputlog.ui.screens.common.composable.bars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.myinputlog.R

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
    hasAddAction: Boolean = false,
    isFormValid: Boolean = false,
    onDelete: () -> Unit = {},
    onSave: () -> Unit = {},
    onAdd: () -> Unit = {},
    navigateUp: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    if (canNavigateBack) {
        TopAppBar(modifier = modifier, title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        }, navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button_content_description)
                )
            }
        }, scrollBehavior = scrollBehavior, actions = {
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
                        Icons.Filled.Done, contentDescription = stringResource(R.string.save_text)
                    )
                }
            }
            if (hasAddAction) {
                IconButton(onClick = onAdd) {
                    Icon(
                        Icons.Filled.Add, contentDescription = stringResource(R.string.add_text)
                    )
                }
            }
        })
    } else {
        TopAppBar(
            modifier = modifier, title = { Text(title) })
    }
}