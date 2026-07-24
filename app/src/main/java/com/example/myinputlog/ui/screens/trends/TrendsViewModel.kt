package com.example.myinputlog.ui.screens.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.R
import com.example.myinputlog.data.local.model.DailyWatchWrapper
import com.example.myinputlog.data.local.model.RegionStat
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.TimeRange
import com.example.myinputlog.ui.models.TrendsPeriodOption
import com.example.myinputlog.ui.models.TrendsTimePeriod
import com.example.myinputlog.ui.models.toChannelUiModel
import com.example.myinputlog.ui.models.toLabelUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val repository: StorageDataRepository
) : ViewModel() {

    val currentCourseId: StateFlow<String> = repository.currentCourseId.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    private val _timePeriod = MutableStateFlow(TrendsTimePeriod.LAST_4_WEEKS)
    private val _channelLimit = MutableStateFlow(5)
    private val _labelLimit = MutableStateFlow(5)

    private val configFlow = combine(
        currentCourseId.filter { it.isNotEmpty() }, _timePeriod, _channelLimit, _labelLimit
    ) { courseId, period, channelLimit, labelLimit ->
        Config(courseId, period, channelLimit, labelLimit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendsUiState: StateFlow<TrendsUiState> = configFlow.flatMapLatest { config ->
        val (currentRange, previousRange) = config.period.getTimeRanges()
        combine(
            timeStatsFlow(config, currentRange, previousRange),
            categoryStatsFlow(config, currentRange)
        ) { timeStats, catStats ->
            mapToUiState(config.period, timeStats, catStats)
        }
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrendsUiState.Loading
    )

    private fun timeStatsFlow(
        config: Config, currentRange: TimeRange, previousRange: TimeRange
    ): Flow<TimeStats> {
        val courseGoalFlow = repository.courses.map { courses ->
            courses.find { it.course.id == config.courseId }?.course?.goalInHours?.times(3600L)
                ?: 0L
        }.distinctUntilChanged()

        return combine(
            courseGoalFlow,
            repository.getBaselineProgress(config.courseId, currentRange.start),
            repository.getDailyWatchStats(config.courseId, currentRange.start, currentRange.end),
            repository.getDailyWatchStats(config.courseId, previousRange.start, previousRange.end),
            ::TimeStats
        )
    }

    private fun categoryStatsFlow(
        config: Config, currentRange: TimeRange
    ): Flow<CategoryStats> {
        return combine(
            repository.getRegionStats(config.courseId, currentRange.start, currentRange.end),
            repository.getLabelStats(config.courseId, currentRange.start, currentRange.end),
            repository.getTopChannelsWithStatsAndLabels(
                config.courseId, currentRange.start, currentRange.end, config.channelLimit
            )
        ) { regions, labels, channels ->
            CategoryStats(
                regions = regions,
                labels = labels.map { it.toLabelUiModel() },
                channels = channels.map { it.toChannelUiModel() })
        }
    }

    private fun mapToUiState(
        period: TrendsTimePeriod, timeStats: TimeStats, catStats: CategoryStats
    ): TrendsUiState {

        var runningTotal = timeStats.baseline
        val progressPoints = timeStats.currentDaily.dailyStats.map { daily ->
            runningTotal += daily.totalSeconds
            val percentage = if (timeStats.goal > 0) {
                (runningTotal.toFloat() / timeStats.goal.toFloat()) * 100f
            } else 0f
            ProgressPoint(daily.date, percentage)
        }

        return TrendsUiState.Success(
            selectedPeriod = period,
            cumulativeProgress = progressPoints,
            years = timeStats.currentDaily.years,
            goalTargetInHours = timeStats.goal / 3600F,
            currentProgressInHours = runningTotal / 3600F,
            currentPeriodDailyStats = timeStats.currentDaily.dailyStats,
            previousPeriodDailyStats = timeStats.previousDaily.dailyStats,
            regionStats = catStats.regions,
            topLabels = catStats.labels.take(_labelLimit.value),
            topChannels = catStats.channels
        )
    }

    val timePeriodOptions = listOf(
        TrendsPeriodOption(TrendsTimePeriod.LAST_7_DAYS, R.string.period_last_7_days),
        TrendsPeriodOption(TrendsTimePeriod.LAST_4_WEEKS, R.string.period_last_4_weeks),
        TrendsPeriodOption(TrendsTimePeriod.LAST_6_MONTHS, R.string.period_last_6_months),
        TrendsPeriodOption(TrendsTimePeriod.LAST_YEAR, R.string.period_last_year),
        TrendsPeriodOption(TrendsTimePeriod.ALL_TIME, R.string.period_all_time)
    )

    fun setTimePeriod(period: TrendsTimePeriod) {
        _timePeriod.value = period
    }

    fun expandChannels() {
        _channelLimit.value = 50
    }
}

private data class Config(
    val courseId: String, val period: TrendsTimePeriod, val channelLimit: Int, val labelLimit: Int
)

private data class TimeStats(
    val goal: Long,
    val baseline: Long,
    val currentDaily: DailyWatchWrapper,
    val previousDaily: DailyWatchWrapper
)

private data class CategoryStats(
    val regions: List<RegionStat>,
    val labels: List<LabelUiModel>,
    val channels: List<ChannelUiModel>
)
