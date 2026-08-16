package com.example.myinputlog.ui.screens.common.composable.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.ui.screens.common.composable.calendar.CalendarStateBuilder.buildCalendarState
import com.example.myinputlog.ui.screens.common.composable.input.YearMonthPicker
import com.example.myinputlog.ui.screens.home.MonthlyStatsResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private const val INITIAL_PAGE_INDEX = 50000
private const val PAGE_COUNT = 100000

@Composable
fun SwipeableCalendar(
    modifier: Modifier = Modifier,
    onDayClicked: (CalendarDay) -> Unit,
    onSummaryClicked: (YearMonth) -> Unit,
    getStatsForMonth: (String) -> Flow<MonthlyStatsResult>
) {
    val pagerState = rememberPagerState(
        initialPage = INITIAL_PAGE_INDEX, pageCount = { PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()

    val baseYearMonth = remember { YearMonth.now() }
    var isPickerOpen by remember { mutableStateOf(false) }
    val currentlyVisibleYearMonth = remember(pagerState.currentPage) {
        val offset = pagerState.currentPage - INITIAL_PAGE_INDEX
        baseYearMonth.plusMonths(offset.toLong())
    }

    HorizontalPager(
        state = pagerState, modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top
    ) { page ->
        val pageYearMonth = remember(page) {
            val offset = page - INITIAL_PAGE_INDEX
            YearMonth.now().plusMonths(offset.toLong())
        }

        val statsForPage by remember(pageYearMonth) {
            getStatsForMonth(pageYearMonth.toString())
        }.collectAsStateWithLifecycle(initialValue = MonthlyStatsResult.Loading)

        val calendarUiState = remember(pageYearMonth, statsForPage) {
            buildCalendarState(pageYearMonth, statsForPage)
        }

        MyInputLogCalendar(
            calendarUiState = calendarUiState,
            onDayClicked = onDayClicked,
            onSummaryClicked = onSummaryClicked,
            onBackClicked = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            onForwardClicked = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            onHeaderClicked = {
                isPickerOpen = true
            })
    }

    if (isPickerOpen) {
        YearMonthPicker(
            initialYearMonth = currentlyVisibleYearMonth,
            onYearMonthSelected = { selectedYearMonth ->
                isPickerOpen = false

                val monthsDiff = ChronoUnit.MONTHS.between(baseYearMonth, selectedYearMonth).toInt()
                val targetPage = INITIAL_PAGE_INDEX + monthsDiff

                coroutineScope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
            },
            onDismissRequest = {
                isPickerOpen = false
            })
    }
}