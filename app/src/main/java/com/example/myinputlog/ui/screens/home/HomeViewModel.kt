package com.example.myinputlog.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.CourseUiModel
import com.example.myinputlog.ui.models.MonthlyStatsUiModel
import com.example.myinputlog.ui.models.mapToCourseUiModel
import com.example.myinputlog.ui.models.toCourseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MonthlyStatsResult {
    data object Loading : MonthlyStatsResult
    data class Success(val data: MonthlyStatsUiModel) : MonthlyStatsResult
    data class Error(val e: Throwable) : MonthlyStatsResult
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StorageDataRepository
) : ViewModel() {
    private val isParty = MutableStateFlow(false)

    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todaySeconds: Flow<Long> = currentCourseId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(0L) else repository.getTodaySecondsFlow(id)
    }

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.courses, currentCourseId, isParty, repository.confettiColors, todaySeconds
    ) { courses, id, party, colors, secondsToday ->

        when {
            courses.isEmpty() -> HomeUiState.Empty

            else -> {
                val current = courses.find { it.course.id == id } ?: courses.first()
                val courseHeader = mapToCourseUiModel(
                    current.toCourseUiModel(), secondsToday
                )
                HomeUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses.map { it.toCourseUiModel() },
                    isParty = party,
                    confettiColors = colors.colors
                )
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), // Save battery
        initialValue = HomeUiState.Loading
    )

    fun confetti() {
        isParty.value = true
    }

    fun confettiStop() {
        isParty.value = false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getStatsForMonth(monthId: String): Flow<MonthlyStatsResult> {
        return currentCourseId.flatMapLatest { courseId ->
            if (courseId.isBlank()) {
                flowOf(MonthlyStatsResult.Loading)
            } else {
                repository.getMonthlyStatsFlow(courseId, monthId).map { stats ->
                    MonthlyStatsResult.Success(stats ?: MonthlyStatsUiModel(id = monthId))
                }
            }
        }
    }

    fun changeCurrentCourseId(newCourse: CourseUiModel) {
        viewModelScope.launch {
            repository.setCurrentCourse(newCourse.id)
        }
    }
}