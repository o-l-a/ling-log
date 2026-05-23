package com.example.myinputlog.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.model.UserCourse
import com.example.myinputlog.data.model.UserMonthlyStats
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.mapToCourseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.abs

sealed interface MonthlyStatsResult {
    data object Loading : MonthlyStatsResult
    data class Success(val data: UserMonthlyStats) : MonthlyStatsResult
    data class Error(val e: Throwable) : MonthlyStatsResult
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StorageDataRepository
) : ViewModel() {
    private val isParty = MutableStateFlow(false)

    private val _monthlyStatsMap = MutableStateFlow<Map<String, MonthlyStatsResult>>(emptyMap())
    val monthlyStatsMap: StateFlow<Map<String, MonthlyStatsResult>> = _monthlyStatsMap.asStateFlow()
    private val activeJobMap = mutableMapOf<String, Job>()

    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.userCourses, currentCourseId, isParty
    ) { courses, id, party ->

        when {
            courses == null -> HomeUiState.Loading
            courses.isEmpty() -> HomeUiState.Empty

            else -> {
                val current = courses.find { it.id == id } ?: courses.first()
                val courseHeader = mapToCourseUiModel(
                    current, 0L
                )
                HomeUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses,
                    isParty = party,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // Save battery
        initialValue = HomeUiState.Loading
    )

    fun onMonthSettled(yearMonth: YearMonth) {
        val monthId = yearMonth.toString()
        val courseId = currentCourseId.value
        if (courseId.isBlank()) return

        val jobKey = "$courseId-$monthId"

        if (activeJobMap.containsKey(jobKey)) {
            cleanupDistancedListeners(yearMonth, courseId)
            return
        }

        activeJobMap[jobKey] = viewModelScope.launch {
            repository.getMonthlyStatsFlow(courseId, monthId).collect { stats ->
                _monthlyStatsMap.update { currentMap ->
                    currentMap + (monthId to MonthlyStatsResult.Success(
                        stats ?: UserMonthlyStats(id = monthId)
                    ))
                }
            }
        }

        cleanupDistancedListeners(yearMonth, courseId)
    }

    private fun cleanupDistancedListeners(currentMonth: YearMonth, courseId: String) {
        val iterator = activeJobMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val entryKey = entry.key

            if (entryKey.startsWith(courseId)) {
                val entryMonthId = entryKey.removePrefix("$courseId-")
                val entryYearMonth = YearMonth.parse(entryMonthId)

                val monthsBetween =
                    java.time.temporal.ChronoUnit.MONTHS.between(currentMonth, entryYearMonth)

                if (abs(monthsBetween) > 2) {
                    Log.d(TAG, "Stopping listener for ${entry.key}")
                    entry.value.cancel()
                    iterator.remove()
                }
            }
        }
    }

    private fun cleanupAllListeners() {
        activeJobMap.forEach { (key, job) ->
            job.cancel()
            Log.d(TAG, "Cancelling listener $key")
        }
        activeJobMap.clear()
        _monthlyStatsMap.value = emptyMap()
    }

    fun confetti() {
        isParty.value = true
    }

    fun confettiStop() {
        isParty.value = false
    }

    fun changeCurrentCourseId(newCourse: UserCourse) {
        cleanupAllListeners()
        viewModelScope.launch {
            repository.setCurrentCourse(newCourse.id)
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}