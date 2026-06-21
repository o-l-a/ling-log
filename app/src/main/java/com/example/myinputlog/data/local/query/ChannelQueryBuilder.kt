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
                SUM(v.durationInSeconds) AS totalTimeInSeconds 
            FROM channels AS c
            LEFT JOIN videos AS v ON c.id = v.channelId AND v.isDeleted = 0
        """.trimIndent()
        )

        sql.andIf(true, "c.isDeleted = 0")
        sql.andIf(true, "c.courseId = ?", courseId)

        if (filters.searchQuery.isNotBlank()) {
            sql.andIf(true, "c.title LIKE ?", "%${filters.searchQuery}%")
        }

        if (filters.selectedLabels.isNotEmpty()) {
            val placeholders = filters.selectedLabels.joinToString(",") { "?" }
            sql.andIf(
                true, """
                    EXISTS (
                        SELECT 1 FROM channel_label_cross_ref clc 
                        WHERE clc.channelId = c.id 
                        AND clc.labelId IN ($placeholders)
                    ) 
                    """.trimIndent(), *filters.selectedLabels.toTypedArray()
            )
        }

        sql.groupBy("c.id")

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
                sql.orderBy("totalVideoCount DESC")
                sql.orderBy("totalTimeInSeconds DESC")
                sql.orderBy("lower(c.title) ASC")
            }
        }

        return sql.build()
    }
}