package com.example.myinputlog.ui.screens.common.composable.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.myinputlog.R
import com.example.myinputlog.ui.models.CourseHeaderUiModel
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.screens.common.formatDurationAsText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseTopAppBar(
    modifier: Modifier = Modifier,
    courseHeader: CourseHeaderUiModel,
    onValueChange: (CourseUiModel) -> Unit,
    options: List<CourseUiModel>,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(modifier = modifier, scrollBehavior = scrollBehavior, colors = colors, title = {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = courseHeader.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.progress,
                    "${courseHeader.percentageText} (${formatDurationAsText(courseHeader.totalTimeInSeconds)}/${courseHeader.goalInHours}h)"
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }, actions = {
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
                        text = { Text(selectionOption.name) }, onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    })
}