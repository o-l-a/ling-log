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
        val goalTargetInHours: Float = 0f,
        val currentProgressInHours: Float = 0f,

        val currentPeriodDailyStats: List<DailyWatchStat> = emptyList(),
        val previousPeriodDailyStats: List<DailyWatchStat> = emptyList(),

        val regionStats: List<RegionStat> = emptyList(),
        val topLabels: List<LabelUiModel> = emptyList(),
        val topChannels: List<ChannelUiModel> = emptyList()
    ) : TrendsUiState
}

data class ProgressPoint(
    val dateString: String, val percentageOfGoal: Float
)
