package com.example.myinputlog.ui.screens.trends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinputlog.data.local.model.RegionStat
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.RankingCategory
import com.example.myinputlog.ui.models.RankingLimit
import com.example.myinputlog.ui.models.TimeRange
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
    private val _rankingCategory = MutableStateFlow(RankingCategory.LABEL)
    private val _rankingLimit = MutableStateFlow(RankingLimit.TOP_3)

    private val configFlow = combine(
        currentCourseId.filter { it.isNotEmpty() }, _timePeriod, _rankingCategory, _rankingLimit
    ) { courseId, period, rankingCategory, rankingLimit ->
        Config(courseId, period, rankingCategory, rankingLimit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendsUiState: StateFlow<TrendsUiState> = configFlow.flatMapLatest { config ->
        val (currentRange, previousRange) = config.period.getTimeRanges()
        combine(
            timeStatsFlow(config, currentRange, previousRange),
            categoryStatsFlow(config, currentRange)
        ) { timeStats, catStats ->
            mapToUiState(config, timeStats, catStats)
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
            repository.getRegionStats(
                config.courseId, currentRange.start, currentRange.end, config.rankingLimit.limit
            ), repository.getLabelStats(
                config.courseId, currentRange.start, currentRange.end, config.rankingLimit.limit
            ), repository.getTopChannelsWithStatsAndLabels(
                config.courseId, currentRange.start, currentRange.end, config.rankingLimit.limit
            )
        ) { regions, labels, channels ->
            CategoryStats(
                regions = regions,
                labels = labels.map { it.toLabelUiModel() },
                channels = channels.map { it.toChannelUiModel() })
        }
    }

    private fun mapToUiState(
        config: Config, timeStats: TimeStats, catStats: CategoryStats
    ): TrendsUiState {
        if (timeStats.currentDaily.dailyStats.isEmpty()) {
            return TrendsUiState.Empty(
                selectedPeriod = config.period,
                selectedRankingCategory = config.rankingCategory,
                selectedRankingLimit = config.rankingLimit,
            )
        }
        val aggregatedData = TrendsDataAggregator.aggregate(config.period, timeStats)
        val totalPoints = timeStats.currentDaily.dailyStats.size
        Log.d(TAG, "Number of points: $totalPoints vs ${aggregatedData.cumulativeProgress.size}")

        return TrendsUiState.Success(
            selectedPeriod = config.period,
            selectedRankingCategory = config.rankingCategory,
            selectedRankingLimit = config.rankingLimit,
            cumulativeProgress = aggregatedData.cumulativeProgress,
            totalPoints = totalPoints,
            years = timeStats.currentDaily.years,
            goalTargetInSeconds = timeStats.goal,
            currentProgressInSeconds = aggregatedData.finalRunningTotal,
            currentPeriodDailyStats = timeStats.currentDaily.dailyStats,
            previousPeriodDailyStats = timeStats.previousDaily.dailyStats,
            regionStats = catStats.regions,
            topLabels = catStats.labels,
            topChannels = catStats.channels,
            currentPeriodSummary = aggregatedData.currentSummary,
            previousPeriodSummary = aggregatedData.previousSummary
        )
    }

    fun setTimePeriod(period: TrendsTimePeriod) {
        _timePeriod.value = period
    }

    fun setRankingCategory(category: RankingCategory) {
        _rankingCategory.value = category
    }

    fun setRankingLimit(limit: RankingLimit) {
        _rankingLimit.value = limit
    }

    companion object {
        private const val TAG = "TrendsViewModel"
    }
}

private data class Config(
    val courseId: String,
    val period: TrendsTimePeriod,
    val rankingCategory: RankingCategory,
    val rankingLimit: RankingLimit
)

private data class CategoryStats(
    val regions: List<RegionStat>,
    val labels: List<LabelUiModel>,
    val channels: List<ChannelUiModel>
)
