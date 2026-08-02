package com.example.myinputlog.ui.screens.trends

import com.example.myinputlog.data.local.model.DailyWatchWrapper
import com.example.myinputlog.ui.models.TrendsTimePeriod
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

sealed interface BucketDescriptor {
    data class Day(val date: LocalDate) : BucketDescriptor
    data class Week(val startDate: LocalDate) : BucketDescriptor
    data class Month(val date: LocalDate) : BucketDescriptor
    data class Year(val year: Int) : BucketDescriptor
}

data class ChartBucketData(
    val currentProgress: List<Long> = emptyList(),
    val previousProgress: List<Long> = emptyList(),
    val descriptors: List<BucketDescriptor> = emptyList()
)

data class AggregatedProgress(
    val cumulativeProgress: List<ProgressPoint>,
    val finalRunningTotal: Long,
    val chartBucketData: ChartBucketData
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

    private data class Bucket(
        val currentSecs: Long, val previousSecs: Long, val desc: BucketDescriptor
    )

    fun aggregate(
        period: TrendsTimePeriod, timeStats: TimeStats
    ): AggregatedProgress {
        val finalRunningTotal =
            timeStats.baseline + timeStats.currentDaily.dailyStats.sumOf { it.totalSeconds }
        val cumulativeProgress =
            getRunningTotal(timeStats.baseline, timeStats.goal, timeStats.currentDaily)

        if (timeStats.goal <= 0) {
            return AggregatedProgress(
                cumulativeProgress = cumulativeProgress,
                finalRunningTotal = finalRunningTotal,
                chartBucketData = ChartBucketData(emptyList(), emptyList(), emptyList())
            )
        }

        val (currentRange, previousRange) = period.getTimeRanges()
        val currentStart = currentRange.start.toLocalDate()
        val previousStart = previousRange.start.toLocalDate()

        val currentDailyMap = timeStats.currentDaily.dailyStats.associate {
            LocalDate.ofEpochDay(it.date) to it.totalSeconds
        }
        val previousDailyMap = timeStats.previousDaily.dailyStats.associate {
            LocalDate.ofEpochDay(it.date) to it.totalSeconds
        }

        val buckets: List<Bucket> = when (period) {
            TrendsTimePeriod.LAST_7_DAYS -> (0L..6L).map { i ->
                val cDate = currentStart.plusDays(i)
                val pDate = previousStart.plusDays(i)
                Bucket(
                    currentSecs = currentDailyMap[cDate] ?: 0L,
                    previousSecs = previousDailyMap[pDate] ?: 0L,
                    desc = BucketDescriptor.Day(cDate)
                )
            }

            TrendsTimePeriod.LAST_4_WEEKS -> (0L..3L).map { i ->
                val cStart = currentStart.plusWeeks(i)
                val pStart = previousStart.plusWeeks(i)
                Bucket(
                    currentSecs = currentDailyMap.sumInRange(cStart, cStart.plusDays(6)),
                    previousSecs = previousDailyMap.sumInRange(pStart, pStart.plusDays(6)),
                    desc = BucketDescriptor.Week(cStart)
                )
            }

            TrendsTimePeriod.LAST_6_MONTHS, TrendsTimePeriod.LAST_YEAR -> {
                val numMonths = if (period == TrendsTimePeriod.LAST_6_MONTHS) 6L else 12L
                (0L until numMonths).map { i ->
                    val cMonth = YearMonth.from(currentStart.plusMonths(i))
                    val pMonth = YearMonth.from(previousStart.plusMonths(i))
                    Bucket(
                        currentSecs = currentDailyMap.sumInMonth(cMonth),
                        previousSecs = previousDailyMap.sumInMonth(pMonth),
                        desc = BucketDescriptor.Month(cMonth.atDay(1))
                    )
                }
            }

            TrendsTimePeriod.ALL_TIME -> {
                val todayYear = LocalDate.now().year
                val minYear = currentDailyMap.keys.minOfOrNull { it.year } ?: todayYear

                (minYear..todayYear).map { year ->
                    val cSecs = currentDailyMap.filterKeys { it.year == year }.values.sum()
                    Bucket(
                        currentSecs = cSecs, previousSecs = 0L, desc = BucketDescriptor.Year(year)
                    )
                }
            }
        }

        return AggregatedProgress(
            cumulativeProgress = cumulativeProgress,
            finalRunningTotal = finalRunningTotal,
            chartBucketData = ChartBucketData(
                currentProgress = buckets.map { it.currentSecs },
                previousProgress = buckets.map { it.previousSecs },
                descriptors = buckets.map { it.desc })
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

    private fun Map<LocalDate, Long>.sumInRange(start: LocalDate, end: LocalDate): Long {
        var sum = 0L
        var current = start
        while (!current.isAfter(end)) {
            sum += this[current] ?: 0L
            current = current.plusDays(1)
        }
        return sum
    }

    private fun Map<LocalDate, Long>.sumInMonth(yearMonth: YearMonth): Long {
        var sum = 0L
        for (day in 1..yearMonth.lengthOfMonth()) {
            sum += this[yearMonth.atDay(day)] ?: 0L
        }
        return sum
    }
}