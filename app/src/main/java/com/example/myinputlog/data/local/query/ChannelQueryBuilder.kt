package com.example.myinputlog.data.local.query

import androidx.sqlite.db.SupportSQLiteQuery
import com.example.myinputlog.ui.screens.media_list.MediaFilters

object ChannelQueryBuilder {
    fun build(courseId: String, filters: MediaFilters): SupportSQLiteQuery {
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
                        SELECT 1 FROM ChannelLabelCrossRef clc 
                        WHERE clc.channelId = c.id 
                        AND clc.labelId IN ($placeholders)
                    ) 
                    """.trimIndent(), *filters.selectedLabels.toTypedArray()
            )
        }

        sql.groupBy("c.id")

        sql.orderBy("totalVideoCount DESC")
        sql.orderBy("totalTimeInSeconds DESC")
        sql.orderBy("c.title ASC")

        return sql.build()
    }
}