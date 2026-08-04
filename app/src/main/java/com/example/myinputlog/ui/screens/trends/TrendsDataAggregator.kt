package com.example.myinputlog.ui.screens.trends

import com.example.myinputlog.data.local.model.DailyWatchWrapper
import com.example.myinputlog.ui.models.TrendsTimePeriod
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PeriodSummary(
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    val totalSeconds: Long = 0L
)

data class AggregatedProgress(
    val cumulativeProgress: List<ProgressPoint>,
    val finalRunningTotal: Long,
    val currentSummary: PeriodSummary,
    val previousSummary: PeriodSummary
)

data class TimeStats(
    val goal: Long,
    val baseline: Long,
    val currentDaily: DailyWatchWrapper,
    val previousDaily: DailyWatchWrapper
)

object TrendsDataAggregator {

    private val zoneId = ZoneId.systemDefault()
    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

    private fun Long.toPercentageOf(goal: Long): Float =
        if (goal > 0) (this.toFloat() / goal.toFloat()) * 100f else 0f

    fun aggregate(
        period: TrendsTimePeriod, timeStats: TimeStats
    ): AggregatedProgress {
        val finalRunningTotal =
            timeStats.baseline + timeStats.currentDaily.dailyStats.sumOf { it.totalSeconds }
        val cumulativeProgress =
            getRunningTotal(timeStats.baseline, timeStats.goal, timeStats.currentDaily)

        if (timeStats.goal <= 0) {
            val today = LocalDate.now()
            return AggregatedProgress(
                cumulativeProgress = cumulativeProgress,
                finalRunningTotal = finalRunningTotal,
                currentSummary = PeriodSummary(today, today, 0L),
                previousSummary = PeriodSummary(today, today, 0L)
            )
        }

        val currentStart: LocalDate
        val currentEnd: LocalDate
        val currentTotal: Long

        val previousStart: LocalDate
        val previousEnd: LocalDate
        val previousTotal: Long

        if (period == TrendsTimePeriod.ALL_TIME) {
            val earliestCurrent = timeStats.currentDaily.dailyStats.minOfOrNull { it.date }
            currentStart = earliestCurrent?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
            currentEnd = LocalDate.now()
            currentTotal = timeStats.currentDaily.dailyStats.sumOf { it.totalSeconds }

            previousStart = currentStart
            previousEnd = currentEnd
            previousTotal = 0L
        } else {
            val (currentRange, previousRange) = period.getTimeRanges()

            currentStart = currentRange.start.toLocalDate()
            currentEnd = currentRange.end.toLocalDate()
            previousStart = previousRange.start.toLocalDate()
            previousEnd = previousRange.end.toLocalDate()

            currentTotal = timeStats.currentDaily.dailyStats.sumOf {
                val date = LocalDate.ofEpochDay(it.date)
                if (date in currentStart..currentEnd) it.totalSeconds else 0L
            }

            previousTotal = timeStats.previousDaily.dailyStats.sumOf {
                val date = LocalDate.ofEpochDay(it.date)
                if (date in previousStart..previousEnd) it.totalSeconds else 0L
            }
        }

        return AggregatedProgress(
            cumulativeProgress = cumulativeProgress,
            finalRunningTotal = finalRunningTotal,
            currentSummary = PeriodSummary(currentStart, currentEnd, currentTotal),
            previousSummary = PeriodSummary(previousStart, previousEnd, previousTotal)
        )
    }

    private fun getRunningTotal(
        baseline: Long, goalSeconds: Long, currentDaily: DailyWatchWrapper
    ): List<ProgressPoint> {
        var runningTotal = baseline
        val rawProgressPoints = currentDaily.dailyStats.map { daily ->
            runningTotal += daily.totalSeconds
            ProgressPoint(daily.date, runningTotal.toPercentageOf(goalSeconds))
        }

        return decimatePoints(rawProgressPoints, currentDaily.years)
    }

    /**
     * Removes points from dataset. Keeps calculated step & required markers.
     */
    private fun decimatePoints(
        points: List<ProgressPoint>, mustKeep: List<Long>, maxSize: Int = 150, targetSize: Int = 90
    ): List<ProgressPoint> {
        if (points.size <= maxSize) return points

        val step = points.size / targetSize
        val milestoneSet = mustKeep.toSet()
        val lastDate = points.lastOrNull()?.date

        return points.filterIndexed { index, point ->
            val isMonthStart = LocalDate.ofEpochDay(point.date).dayOfMonth == 1
            index % step == 0 || point.date in milestoneSet || isMonthStart || point.date == lastDate
        }.sortedBy { it.date }
    }
}