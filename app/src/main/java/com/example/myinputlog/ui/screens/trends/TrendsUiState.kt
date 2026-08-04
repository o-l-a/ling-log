package com.example.myinputlog.ui.screens.trends

import com.example.myinputlog.data.local.model.DailyWatchStat
import com.example.myinputlog.data.local.model.RegionStat
import com.example.myinputlog.ui.models.ChannelUiModel
import com.example.myinputlog.ui.models.LabelUiModel
import com.example.myinputlog.ui.models.TrendsTimePeriod

sealed interface TrendsUiState {
    data object Loading : TrendsUiState
    data object Error : TrendsUiState
    data class Success(
        val selectedPeriod: TrendsTimePeriod = TrendsTimePeriod.LAST_4_WEEKS,

        val cumulativeProgress: List<ProgressPoint> = emptyList(),
        val totalPoints: Int = 0,
        val years: List<Long>,
        val goalTargetInSeconds: Long = 0L,
        val currentProgressInSeconds: Long = 0L,

        val currentPeriodDailyStats: List<DailyWatchStat> = emptyList(),
        val previousPeriodDailyStats: List<DailyWatchStat> = emptyList(),

        val regionStats: List<RegionStat> = emptyList(),
        val topLabels: List<LabelUiModel> = emptyList(),
        val topChannels: List<ChannelUiModel> = emptyList(),
        val currentPeriodSummary: PeriodSummary = PeriodSummary(),
        val previousPeriodSummary: PeriodSummary = PeriodSummary()
    ) : TrendsUiState
}

data class ProgressPoint(
    val date: Long, val percentageOfGoal: Float
)
