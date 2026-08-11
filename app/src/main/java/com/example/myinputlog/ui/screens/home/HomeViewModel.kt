package com.example.myinputlog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.data.utils.StringProvider
import com.example.myinputlog.ui.models.MonthlyDashboardUiModel
import com.example.myinputlog.ui.models.MonthlyStatsUiModel
import com.example.myinputlog.ui.models.TopItemsUiModel
import com.example.myinputlog.ui.models.mapToCourseUiModel
import com.example.myinputlog.ui.models.toChannelUiModel
import com.example.myinputlog.ui.models.toCourseUiModel
import com.example.myinputlog.ui.models.toLabelUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

sealed interface MonthlyStatsResult {
    data object Loading : MonthlyStatsResult
    data class Success(val data: MonthlyDashboardUiModel) : MonthlyStatsResult
    data class Error(val e: Throwable) : MonthlyStatsResult
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StorageDataRepository, private val stringProvider: StringProvider
) : ViewModel() {
    private val isParty = MutableStateFlow(false)

    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todaySeconds: Flow<Long> = currentCourseId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(0L) else repository.getTodaySecondsFlow(id)
    }.flowOn(Dispatchers.Default)

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.courses, currentCourseId, isParty, repository.confettiColors, todaySeconds
    ) { courses, id, party, colors, secondsToday ->

        when {
            courses.isEmpty() -> HomeUiState.Empty

            else -> {
                val current = courses.find { it.course.id == id } ?: courses.first()
                val courseHeader = mapToCourseUiModel(
                    current.toCourseUiModel(stringProvider), secondsToday
                )
                HomeUiState.Success(
                    courseHeader = courseHeader,
                    userCourses = courses.map { it.toCourseUiModel(stringProvider) },
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
    fun getMonthlyDashboard(monthId: String): Flow<MonthlyStatsResult> {
        return currentCourseId.flatMapLatest { courseId ->
            if (courseId.isBlank()) {
                return@flatMapLatest flowOf(MonthlyStatsResult.Loading)
            }

            val yearMonth = YearMonth.parse(monthId)
            val zoneId = ZoneId.systemDefault()
            val start = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zoneId).toInstant()
                .toEpochMilli()

            val limit = 20

            val statsFlow = repository.getMonthlyStatsFlow(courseId, monthId)
            val channelsFlow =
                repository.getTopChannelsWithStatsAndLabels(courseId, start, end, limit)
            val labelsFlow = repository.getSimpleLabelStats(courseId, start, end, limit)
            val countsFlow = repository.getChannelLabelCounts(courseId, start, end)

            combine(
                statsFlow, channelsFlow, labelsFlow, countsFlow
            ) { stats, topChannelsDb, topLabelsDb, counts ->
                val safeStats = stats ?: MonthlyStatsUiModel(id = monthId)

                val channelUiItems = topChannelsDb.map { it.toChannelUiModel() }
                val labelUiItems = topLabelsDb.map { it.toLabelUiModel() }

                val extraChannelsCount =
                    (counts.channelCount - channelUiItems.size).coerceAtLeast(0)
                val extraLabelsCount = (counts.labelCount - labelUiItems.size).coerceAtLeast(0)

                MonthlyStatsResult.Success(
                    MonthlyDashboardUiModel(
                        stats = safeStats,
                        topChannels = TopItemsUiModel(channelUiItems, extraChannelsCount),
                        topLabels = TopItemsUiModel(labelUiItems, extraLabelsCount)
                    )
                )
            }
        }.flowOn(Dispatchers.Default)
    }
}