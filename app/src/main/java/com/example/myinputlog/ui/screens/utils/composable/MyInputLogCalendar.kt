package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.background
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
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MyInputLogCalendar(
    modifier: Modifier = Modifier,
    yearMonth: YearMonth,
    dailyTotalTimes: List<Int>
) {
    val daysOfWeek = DayOfWeek.values()
    val shortWeekdays = daysOfWeek.map {
        it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).first().toString()
            .uppercase(Locale.ROOT)
    }

    Column(
        modifier = modifier.padding(8.dp)
    ) {
        // Display the header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /*TODO*/ }) {
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
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
            }
        }
        // Display the weekdays
        Row(modifier = Modifier.fillMaxWidth()) {
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

        Spacer(modifier = Modifier.height(4.dp))

        // Display the calendar days
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

        val calendarDailyTotalTimes = (0 until leadingEmptyDays).map { -1 } +
                dailyTotalTimes +
                (0 until trailingEmptyDays).map { -1 }

        val calendarItems = dayItems.zip(calendarDailyTotalTimes)

        Column(
            modifier = Modifier.fillMaxWidth(),
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
                                        -1 -> Color.Transparent
                                        0 -> Color.Transparent
                                        else -> MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = (day.second.toFloat() / 80).coerceAtMost(1.0f)
                                        )
                                    }
                                )
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                                text = day.first
                            )
                            Text(
                                modifier = Modifier.padding(MaterialTheme.spacing.extraExtraSmall),
                                text = if (day.second >= 0) "${day.second}m" else "",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
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
                dailyTotalTimes = (0 until 30).map { (it * 4) }.shuffled()
            )
        }
    }
}