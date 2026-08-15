package com.example.myinputlog.data.local.query

import androidx.sqlite.db.SupportSQLiteQuery
import com.example.myinputlog.data.local.query.SortOptions.CHANNEL_TITLE_ASC
import com.example.myinputlog.data.local.query.SortOptions.CHANNEL_TITLE_DESC
import com.example.myinputlog.data.local.query.SortOptions.TOTAL_TIME_ASC
import com.example.myinputlog.data.local.query.SortOptions.TOTAL_TIME_DESC
import com.example.myinputlog.data.local.query.SortOptions.VIDEO_COUNT_ASC
import com.example.myinputlog.data.local.query.SortOptions.VIDEO_COUNT_DESC
import com.example.myinputlog.ui.screens.media_list.MediaFilters

object ChannelQueryBuilder {
    fun build(
        courseId: String, filters: MediaFilters, sort: SortOptions = SortOptions.DEFAULT
    ): SupportSQLiteQuery {
        val sql = QueryBuilder(
            """
            SELECT 
                c.*, 
                COUNT(v.id) AS totalVideoCount, 
                SUM(v.durationInSeconds) AS totalTimeInSeconds,
                MIN(v.watchedOn) AS firstWatchedOn
            FROM channels AS c
            LEFT JOIN videos AS v ON c.id = v.channelId AND v.isDeleted = 0
        """.trimIndent()
        )
        applyFilters(sql, courseId, filters)
        sql.groupBy("c.id")
        applySort(sql, sort)

        return sql.build()
    }

    fun buildCount(
        courseId: String, filters: MediaFilters, sort: SortOptions = SortOptions.DEFAULT
    ): SupportSQLiteQuery {
        val sql = QueryBuilder(
            """
            SELECT COUNT(c.id)
            FROM channels AS c
        """.trimIndent()
        )
        applyFilters(sql, courseId, filters)

        return sql.build()
    }

    private fun applyFilters(sql: QueryBuilder, courseId: String, filters: MediaFilters) {
        sql.andIf(true, "c.isDeleted = 0")
        sql.andIf(true, "c.courseId = ?", courseId)

        if (filters.searchQuery.isNotBlank()) {
            sql.andIf(true, "c.title LIKE ?", "%${filters.searchQuery}%")
        }

        if (filters.hasLabelFilter()) {
            val labelConditions = buildList {
                if (filters.selectedLabels.isNotEmpty()) {
                    val placeholders = filters.selectedLabels.joinToString(",") { "?" }
                    add(
                        """
                        EXISTS (
                            SELECT 1 FROM channel_label_cross_ref clc 
                            WHERE clc.channelId = c.id 
                            AND clc.labelId IN ($placeholders)
                        )
                        """.trimIndent()
                    )
                }

                if (filters.unassignedLabelSelected) {
                    add(
                        """
                        NOT EXISTS (
                            SELECT 1 FROM channel_label_cross_ref clc
                            WHERE clc.channelId = c.id
                        )
                        """.trimIndent()
                    )
                }
            }

            sql.andIf(
                condition = labelConditions.isNotEmpty(),
                sql = labelConditions.joinToString(separator = " OR ", prefix = "(", postfix = ")"),
                bindArgs = filters.selectedLabels.toTypedArray()
            )
        }

        if (filters.hasCountryFilter()) {
            val countryConditions = buildList {
                if (filters.selectedCountries.isNotEmpty()) {
                    val placeholders = filters.selectedCountries.joinToString(",") { "?" }
                    add("c.defaultLanguage IN ($placeholders)".trimIndent())
                }
                if (filters.unassignedCountrySelected) {
                    add("c.defaultLanguage IS NULL")
                }
            }

            sql.andIf(
                condition = countryConditions.isNotEmpty(), sql = countryConditions.joinToString(
                    separator = " OR ", prefix = "(", postfix = ")"
                ), bindArgs = filters.selectedCountries.toTypedArray()
            )
        }
    }

    private fun applySort(sql: QueryBuilder, sort: SortOptions) {
        when (sort) {
            CHANNEL_TITLE_DESC -> {
                sql.orderBy("lower(c.title) DESC")
            }

            CHANNEL_TITLE_ASC -> {
                sql.orderBy("lower(c.title) ASC")
            }

            VIDEO_COUNT_DESC -> {
                sql.orderBy("totalVideoCount DESC")
                sql.orderBy("lower(c.title) ASC")
            }

            VIDEO_COUNT_ASC -> {
                sql.orderBy("totalVideoCount ASC")
                sql.orderBy("lower(c.title) ASC")
            }

            TOTAL_TIME_DESC -> {
                sql.orderBy("totalTimeInSeconds DESC")
                sql.orderBy("lower(c.title) ASC")
            }

            TOTAL_TIME_ASC -> {
                sql.orderBy("totalTimeInSeconds ASC")
                sql.orderBy("lower(c.title) ASC")
            }

            else -> {
                sql.orderBy("totalTimeInSeconds DESC")
                sql.orderBy("totalVideoCount DESC")
                sql.orderBy("lower(c.title) ASC")
            }
        }
    }
}