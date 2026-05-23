package com.example.myinputlog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.utils.DateUtils.toDayKey
import com.example.myinputlog.ui.models.mapToCourseUiModel
import com.example.myinputlog.ui.screens.home.CalendarStateBuilder.buildCalendarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Date
import javax.inject.Inject

sealed interface MonthlyStatsResult {
    data object Loading : MonthlyStatsResult
    data class Success(val data: UserMonthlyStats) : MonthlyStatsResult
    data class Error(val e: Throwable) : MonthlyStatsResult
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StorageDataRepository
) : ViewModel() {
    private val selectedYearMonth = MutableStateFlow(YearMonth.now())
    private val isParty = MutableStateFlow(false)

    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    private val uiControlFlow = combine(
        selectedYearMonth, isParty
    ) { month, party ->
        month to party
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthlyStats: StateFlow<MonthlyStatsResult> =
        combine(currentCourseId, selectedYearMonth) { courseId, month ->
            courseId to month
        }.flatMapLatest { (courseId, month) ->
            flow {
                emit(MonthlyStatsResult.Loading)
                try {
                    val monthId = month.toString()
                    val data = repository.getMonthlyStats(courseId, monthId)
                        ?: UserMonthlyStats(id = monthId)
                    emit(MonthlyStatsResult.Success(data))
                } catch (e: Exception) {
                    emit(MonthlyStatsResult.Error(e))
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthlyStatsResult.Loading
        )

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.userCourses, currentCourseId, monthlyStats, uiControlFlow
    ) { courses, id, statsRes, controls ->

        val (month, party) = controls
        when {
            courses == null -> HomeUiState.Loading
            courses.isEmpty() -> HomeUiState.Empty
            statsRes is MonthlyStatsResult.Error -> HomeUiState.NetworkError

            else -> {
                val current = courses.find { it.id == id } ?: courses.first()
                val monthlyStats = (statsRes as? MonthlyStatsResult.Success)?.data
                val dayStats = monthlyStats?.days?.getOrDefault(Date().toDayKey(), null)
                val calendarUiState = buildCalendarState(month, statsRes)
                val courseHeader = mapToCourseUiModel(
                    current, dayStats?.totalTimeInSeconds ?: 0L
                )

                HomeUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses,
                    selectedYearMonth = month,
                    isParty = party,
                    calendarState = calendarUiState,
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
            repository.setCurrentCourse(newCourse.id)
        }
    }
}