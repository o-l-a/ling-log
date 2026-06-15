package com.example.myinputlog.ui.screens.common.composable.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.screens.common.ext.conditional
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing

@Composable
fun MyInputLogCalendar(
    modifier: Modifier = Modifier,
    calendarUiState: CalendarUiState,
    onBackClicked: () -> Unit,
    onForwardClicked: () -> Unit
) {
    Column(
        modifier = modifier.padding(MaterialTheme.spacing.small)
    ) {
        CalendarHeader(
            monthName = calendarUiState.monthName,
            onBackClicked = onBackClicked,
            onForwardClicked = onForwardClicked
        )
        CalendarWeekdays(shortWeekdays = calendarUiState.weekdays)
        Spacer(modifier = Modifier.height(4.dp))
        CalendarEntries(calendarUiState.isLoading, calendarUiState.calendarItems)
    }
}

@Composable
fun CalendarHeader(
    modifier: Modifier = Modifier,
    monthName: String,
    onBackClicked: () -> Unit,
    onForwardClicked: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = null)
        }
        Text(
            text = monthName,
        )
        IconButton(onClick = onForwardClicked) {
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun CalendarWeekdays(
    modifier: Modifier = Modifier, shortWeekdays: List<String>
) {
    Row(modifier = modifier.fillMaxWidth()) {
        shortWeekdays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun CalendarEntries(
    isLoading: Boolean, calendarItems: List<CalendarDay>
) {
    calendarItems.chunked(7).forEach { weekItems ->
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            weekItems.forEach { day ->
                Column(
                    modifier = Modifier
                        .padding(
                            horizontal = MaterialTheme.spacing.extraSmall,
                            vertical = MaterialTheme.spacing.small
                        )
                        .clip(RoundedCornerShape(MaterialTheme.spacing.small))
                        .conditional(!isLoading, {
                            background(
                                when (day.totalMinutes) {
                                    0L -> Color.Transparent
                                    else -> MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = day.alpha
                                    )
                                }
                            )
                        })
                        .conditional(!isLoading, {
                            border(
                                width = MaterialTheme.spacing.tiny,
                                color = if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                    MaterialTheme.spacing.small
                                )
                            )
                        })
                        .weight(1f), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                        text = day.dayNumber,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                        text = day.text,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewMyInputLogCalendar() {
    MyInputLogTheme {
        Surface {
            MyInputLogCalendar(
                calendarUiState = CalendarUiState(
                monthName = "April 2026",
                weekdays = listOf(),
                calendarItems = listOf(),
                loadingCalendarItems = listOf(),
                today = 18,
                isLoading = false
            ), onBackClicked = {}, onForwardClicked = {})
        }
    }
}