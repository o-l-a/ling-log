package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MyInputLogCalendar(
    modifier: Modifier = Modifier,
    yearMonth: YearMonth,
    dailyTotalTimes: List<Int>,
    onBackClicked: () -> Unit,
    onForwardClicked: () -> Unit,
    isLoading: Boolean = false
) {
    val daysOfWeek = DayOfWeek.values()
    val shortWeekdays = daysOfWeek.map {
        it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).first().toString()
            .uppercase(Locale.ROOT)
    }
    val daysOfMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
    val leadingEmptyDays = (firstDayOfWeek - 1 + 7) % 7

    val trailingEmptyDays = if ((leadingEmptyDays + daysOfMonth) % 7 != 0) {
        7 - ((leadingEmptyDays + daysOfMonth) % 7)
    } else {
        0
    }

    val dayItems = (0 until leadingEmptyDays).map { "" } +
            (1..daysOfMonth).map { it.toString() } +
            (0 until trailingEmptyDays).map { "" }

    val calendarDailyTotalTimes = when (isLoading) {
        false -> (0 until leadingEmptyDays).map { 0 } +
                dailyTotalTimes +
                (0 until trailingEmptyDays).map { 0 }

        true -> List(dayItems.size) { 0 }
    }

    val calendarItems = dayItems.zip(calendarDailyTotalTimes)

    Column(
        modifier = modifier.padding(MaterialTheme.spacing.small)
    ) {
        CalendarHeader(
            yearMonth = yearMonth,
            onBackClicked = onBackClicked,
            onForwardClicked = onForwardClicked
        )
        CalendarWeekdays(shortWeekdays = shortWeekdays)
        Spacer(modifier = Modifier.height(4.dp))
        if (isLoading) {
            LoadingCalendarDays(calendarItems = calendarItems)
        } else {
            CalendarDays(
                calendarItems = calendarItems,
                today = if (yearMonth == YearMonth.now()) LocalDate.now().dayOfMonth else -1
            )
        }
    }
}

@Composable
fun CalendarHeader(
    modifier: Modifier = Modifier,
    yearMonth: YearMonth,
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
            text = yearMonth.month.getDisplayName(
                TextStyle.FULL_STANDALONE,
                Locale.getDefault()
            ).replaceFirstChar {
                it.titlecase(Locale.getDefault())
            } + " " + yearMonth.year.toString(),
        )
        IconButton(onClick = onForwardClicked) {
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun CalendarWeekdays(
    modifier: Modifier = Modifier,
    shortWeekdays: List<String>
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
fun CalendarDays(
    modifier: Modifier = Modifier,
    calendarItems: List<Pair<String, Int>>,
    today: Int
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
                            .background(
                                when (day.second) {
                                    0 -> Color.Transparent
                                    else -> MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = (day.second.toFloat() / 90).coerceIn(0.2F, 1.0F)
                                    )
                                }
                            )
                            .border(
                                width = MaterialTheme.spacing.tiny,
                                color = if (day.first.toIntOrNull() == today) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(MaterialTheme.spacing.small)
                            )
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                            text = day.first,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                            text = if (day.second > 0) "${day.second}m" else "",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingCalendarDays(
    modifier: Modifier = Modifier,
    calendarItems: List<Pair<String, Int>>
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(alpha = 0.2F),
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
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                                text = day.first,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                                text = "",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
        LoadingBox(modifier = Modifier.padding(bottom = MaterialTheme.spacing.large))
    }
}

@Preview
@Composable
fun PreviewMyInputLogCalendar() {
    val currentYearMonth = YearMonth.now()
    MyInputLogTheme {
        Surface {
            MyInputLogCalendar(
                yearMonth = currentYearMonth,
                dailyTotalTimes = (0 until 30).map { (it * 4) }.shuffled(),
                onBackClicked = {},
                onForwardClicked = {}
            )
        }
    }
}