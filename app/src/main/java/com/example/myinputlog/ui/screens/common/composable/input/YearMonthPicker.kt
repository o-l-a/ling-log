package com.example.myinputlog.ui.screens.common.composable.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.myinputlog.R
import com.example.myinputlog.ui.theme.spacing
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun YearMonthPicker(
    initialYearMonth: YearMonth,
    onYearMonthSelected: (YearMonth) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentYear by remember { mutableIntStateOf(initialYearMonth.year) }
    var currentMonth by remember { mutableStateOf(initialYearMonth.month) }

    val localizedMonthNames = remember {
        val defaultLocale = Locale.getDefault()
        Month.entries.associateWith { month ->
            month.getDisplayName(TextStyle.SHORT, defaultLocale)
        }
    }

    AlertDialog(onDismissRequest = onDismissRequest, modifier = modifier, text = {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentYear-- }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous year"
                    )
                }
                Text(
                    text = currentYear.toString(), style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { currentYear++ }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next year"
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(Month.entries) { month ->
                    val isSelected = month == currentMonth
                    FilterChip(
                        selected = isSelected, onClick = { currentMonth = month }, label = {
                        Text(
                            text = localizedMonthNames[month].orEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }, modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }, confirmButton = {
        TextButton(
            onClick = {
                onYearMonthSelected(YearMonth.of(currentYear, currentMonth))
            }) {
            Text(stringResource(R.string.ok_text))
        }
    }, dismissButton = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            TextButton(
                onClick = {
                    val today = YearMonth.now()
                    currentYear = today.year
                    currentMonth = today.month
                }) {
                Text(stringResource(R.string.today_text))
            }
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel_text))
            }
        }
    })
}