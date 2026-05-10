package com.example.myinputlog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.model.CourseStatistics
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.service.AccountService
import com.example.myinputlog.data.service.impl.DefaultPreferenceStorageService
import com.example.myinputlog.data.service.impl.DefaultStorageService
import com.example.myinputlog.ui.models.mapToCourseHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

sealed interface StatsResult {
    data object Loading : StatsResult
    data class Success(val stats: CourseStatistics) : StatsResult
    data class Error(val e: Throwable) : StatsResult
}

sealed interface CalendarResult {
    data object Loading : CalendarResult
    data class Success(val data: List<Long>) : CalendarResult
    data class Error(val e: Throwable) : CalendarResult
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storageService: DefaultStorageService,
    private val preferenceStorageService: DefaultPreferenceStorageService,
    accountService: AccountService
) : ViewModel() {
    private val userIdFlow = accountService.currentUser.map { it.id }
    private val userCoursesFlow = storageService.userCourses
    private val currentIdFlow = preferenceStorageService.currentCourseId
    private val selectedYearMonth = MutableStateFlow(YearMonth.now())
    private val isParty = MutableStateFlow(false)

    val currentCourseId: StateFlow<String> = preferenceStorageService.currentCourseId.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    private val sessionFlow = combine(userIdFlow, currentIdFlow) { uid, cid ->
        uid to cid
    }

    private val uiControlFlow = combine(
        selectedYearMonth, isParty
    ) { month, party ->
        month to party
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val statsWorker = sessionFlow.flatMapLatest { (uid, cid) ->
        flow {
            emit(StatsResult.Loading)
            try {
                val stats = storageService.getCourseStatistics(uid, cid)
                emit(StatsResult.Success(stats))
            } catch (e: Exception) {
                emit(StatsResult.Error(e))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val calendarWorker = combine(sessionFlow, selectedYearMonth) { (uid, cid), month ->
        Triple(uid, cid, month)
    }.flatMapLatest { (uid, cid, month) ->
        flow {
            emit(CalendarResult.Loading)
            try {
                val data = storageService.getMonthlyAggregateData(uid, cid, month)
                emit(CalendarResult.Success(data))
            } catch (e: Exception) {
                emit(CalendarResult.Error(e))
            }
        }
    }

    val homeUiState: StateFlow<HomeUiState> = combine(
        userCoursesFlow, currentIdFlow, statsWorker, calendarWorker, uiControlFlow
    ) { courses, id, statsRes, calendarRes, controls ->

        val (month, party) = controls
        when {
            courses == null -> HomeUiState.Loading
            courses.isEmpty() -> HomeUiState.Empty
            statsRes is StatsResult.Error -> HomeUiState.NetworkError

            else -> {
                val current = courses.find { it.id == id } ?: courses.first()
                val courseStatistics =
                    (statsRes as? StatsResult.Success)?.stats ?: CourseStatistics()
                val courseHeader = mapToCourseHeader(current, courseStatistics)

                HomeUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses,
                    selectedYearMonth = month,
                    isParty = party,
                    courseStatistics = courseStatistics,
                    monthlyAggregateData = (calendarRes as? CalendarResult.Success)?.data
                        ?: emptyList(),
                    isCalendarLoading = calendarRes is CalendarResult.Loading
                )
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // Save battery
        initialValue = HomeUiState.Loading
    )

    fun nextMonth() {
        selectedYearMonth.update { it.plusMonths(1) }
    }

    fun previousMonth() {
        selectedYearMonth.update { it.minusMonths(1) }
    }

    fun confetti() {
        isParty.value = true
    }

    fun confettiStop() {
        isParty.value = false
    }

    fun changeCurrentCourseId(newCourse: UserCourse) {
        viewModelScope.launch {
            preferenceStorageService.saveCurrentCourseId(newCourse.id)
        }
    }
}