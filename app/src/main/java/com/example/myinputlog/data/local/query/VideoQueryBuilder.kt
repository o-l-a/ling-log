package com.example.myinputlog.data.local.query

import androidx.sqlite.db.SupportSQLiteQuery
import com.example.myinputlog.ui.screens.media_list.MediaFilters

object VideoQueryBuilder {
    fun build(courseId: String, filters: MediaFilters): SupportSQLiteQuery {
        val sql = QueryBuilder("SELECT * FROM videos")

        sql.andIf(true, "isDeleted = 0")
        sql.andIf(true, "courseId = ?", courseId)

        sql.andIf(
            filters.searchQuery.isNotBlank(), "title LIKE ?", "%${filters.searchQuery}%"
        )

        if (filters.selectedChannels.isNotEmpty()) {
            val placeholders = filters.selectedChannels.joinToString(",") { "?" }
            sql.andIf(
                true, "channelId IN ($placeholders)", *filters.selectedChannels.toTypedArray()
            )
        }

        if (filters.selectedLabels.isNotEmpty()) {
            val placeholders = filters.selectedLabels.joinToString(",") { "?" }
            sql.andIf(
                true, """
                    AND EXISTS (
                        SELECT 1 FROM VideoLabelCrossRef vlc 
                        WHERE vlc.videoId = videos.id 
                        AND vlc.labelId IN ($placeholders)
                    ) 
                    """.trimIndent(), *filters.selectedLabels.toTypedArray()
            )
        }

        return sql.build(orderBy = "watchedOn DESC, lastUpdated DESC")
    }
}