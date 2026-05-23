package com.example.myinputlog.ui.screens.utils.composable.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.myinputlog.ui.screens.home.MonthlyStatsResult
import com.example.myinputlog.ui.screens.utils.composable.calendar.CalendarStateBuilder.buildCalendarState
import kotlinx.coroutines.launch
import java.time.YearMonth

private const val INITIAL_PAGE_INDEX = 50000
private const val PAGE_COUNT = 100000

@Composable
fun SwipeableCalendar(
    modifier: Modifier = Modifier,
    selectedCourseId: String,
    monthlyStatsMap: Map<String, MonthlyStatsResult>,
    onMonthSettled: (YearMonth) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = INITIAL_PAGE_INDEX, pageCount = { PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.settledPage, selectedCourseId) {
        val offset = pagerState.settledPage - INITIAL_PAGE_INDEX
        val settledMonth = YearMonth.now().plusMonths(offset.toLong())
        onMonthSettled(settledMonth)
    }

    HorizontalPager(
        state = pagerState, modifier = modifier.fillMaxWidth()
    ) { page ->
        val pageYearMonth = remember(page) {
            val offset = page - INITIAL_PAGE_INDEX
            YearMonth.now().plusMonths(offset.toLong())
        }

        val statsForPage = monthlyStatsMap[pageYearMonth.toString()] ?: MonthlyStatsResult.Loading

        val calendarUiState = remember(pageYearMonth, statsForPage) {
            buildCalendarState(pageYearMonth, statsForPage)
        }

        MyInputLogCalendar(
            calendarUiState = calendarUiState,
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
        )
    }
}