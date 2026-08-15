package com.example.myinputlog.ui.screens.common.composable.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.myinputlog.ui.screens.common.composable.channel.SmallChannelRepresentationRow
import com.example.myinputlog.ui.screens.common.composable.label.SmallLabelChipRow
import com.example.myinputlog.ui.screens.common.ext.conditional
import com.example.myinputlog.ui.theme.spacing

@Composable
fun MyInputLogCalendar(
    modifier: Modifier = Modifier,
    calendarUiState: CalendarUiState,
    onDayClicked: (CalendarDay) -> Unit,
    onBackClicked: () -> Unit,
    onForwardClicked: () -> Unit,
    onHeaderClicked: () -> Unit
) {
    Column(
        modifier = modifier.padding(MaterialTheme.spacing.small)
    ) {
        CalendarHeader(
            monthName = calendarUiState.monthName,
            onBackClicked = onBackClicked,
            onForwardClicked = onForwardClicked,
            onHeaderClicked = onHeaderClicked
        )
        CalendarWeekdays(shortWeekdays = calendarUiState.weekdays)
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
        CalendarEntries(calendarUiState.isLoading, calendarUiState.calendarItems, onDayClicked)
        SmallLabelChipRow(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.extraSmall)
                .padding(top = MaterialTheme.spacing.smallPlus),
            labels = calendarUiState.topLabels.items,
            extraItemCount = calendarUiState.topLabels.extraItemCount.toInt()
        )
        SmallChannelRepresentationRow(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.extraSmall)
                .padding(top = MaterialTheme.spacing.small),
            channels = calendarUiState.topChannels.items,
            extraItemCount = calendarUiState.topChannels.extraItemCount.toInt()
        )
    }
}

@Composable
fun CalendarHeader(
    modifier: Modifier = Modifier,
    monthName: String,
    onBackClicked: () -> Unit,
    onForwardClicked: () -> Unit,
    onHeaderClicked: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = null)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(MaterialTheme.spacing.small))
                .clickable(enabled = true, onClick = onHeaderClicked)
        ) {
            Text(
                text = monthName, modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.extraExtraSmall
                    )
            )
        }
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
    isLoading: Boolean, calendarItems: List<CalendarDay>, onDayClicked: (CalendarDay) -> Unit
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
                        .clickable(
                            enabled = day.date != null && day.totalMinutes > 0L, onClick = {
                                onDayClicked(day)
                            })
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