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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.ui.screens.home.MonthlyStatsResult
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class CalendarDay(
    val dayNumber: Int = 0, val totalMinutes: Long = 0L
)

@Composable
fun MyInputLogCalendar(
    modifier: Modifier = Modifier,
    yearMonth: YearMonth,
    monthlyStatsResult: MonthlyStatsResult,
    onBackClicked: () -> Unit,
    onForwardClicked: () -> Unit
) {
    val daysOfWeek = DayOfWeek.entries.toTypedArray()
    val shortWeekdays = daysOfWeek.map {
        it.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale).first().toString()
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

    val monthlyMap = when (monthlyStatsResult) {
        is MonthlyStatsResult.Success -> {
            monthlyStatsResult.data.days
        }

        else -> emptyMap()
    }

    val calendarItems = (0 until leadingEmptyDays).map { CalendarDay() } + (1..daysOfMonth).map {
        CalendarDay(
            dayNumber = it, totalMinutes =
                ((monthlyMap["day_${it}"]?.totalTimeInSeconds?.toFloat()?.div(60)) ?: 0F).toLong()
        )
    } + (0 until trailingEmptyDays).map { CalendarDay() }

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
        when (monthlyStatsResult) {
            is MonthlyStatsResult.Success -> {
                CalendarDays(
                    calendarItems = calendarItems,
                    today = if (yearMonth == YearMonth.now()) LocalDate.now().dayOfMonth else -1
                )
            }

            else -> {
                LoadingCalendarDays(calendarItems = calendarItems)
            }
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
                TextStyle.FULL_STANDALONE, LocalLocale.current.platformLocale
            ).replaceFirstChar {
                it.titlecase(LocalLocale.current.platformLocale)
            } + " " + yearMonth.year.toString(),
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
fun CalendarDays(
    modifier: Modifier = Modifier, calendarItems: List<CalendarDay>, today: Int
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
                                when (day.totalMinutes) {
                                    0L -> Color.Transparent
                                    else -> MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = (day.totalMinutes.toFloat() / 90).coerceIn(0.2F, 1.0F)
                                    )
                                }
                            )
                            .border(
                                width = MaterialTheme.spacing.tiny,
                                color = if (day.dayNumber == today) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(MaterialTheme.spacing.small)
                            )
                            .weight(1f), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                            text = day.dayNumber.toString(),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                            text = if (day.totalMinutes > 0) "${day.totalMinutes}m" else "",
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
    modifier: Modifier = Modifier, calendarItems: List<CalendarDay>
) {
    Box(
        modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center
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
                                text = day.dayNumber.toString(),
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
                monthlyStatsResult = MonthlyStatsResult.Success(UserMonthlyStats()),
                onBackClicked = {},
                onForwardClicked = {})
        }
    }
}