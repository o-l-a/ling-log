package com.example.myinputlog.data.local.query

import androidx.sqlite.db.SupportSQLiteQuery
import com.example.myinputlog.data.local.query.SortOptions.CHANNEL_TITLE_ASC
import com.example.myinputlog.data.local.query.SortOptions.CHANNEL_TITLE_DESC
import com.example.myinputlog.data.local.query.SortOptions.TITLE_ASC
import com.example.myinputlog.data.local.query.SortOptions.TITLE_DESC
import com.example.myinputlog.data.local.query.SortOptions.WATCH_DATE_ASC
import com.example.myinputlog.data.local.query.SortOptions.WATCH_DATE_DESC
import com.example.myinputlog.ui.screens.media_list.MediaFilters

object VideoQueryBuilder {
    fun build(
        courseId: String, filters: MediaFilters, sort: SortOptions = SortOptions.DEFAULT
    ): SupportSQLiteQuery {
        val sql = QueryBuilder(
            """
                SELECT * FROM videos AS v 
                LEFT JOIN channels AS c ON c.id = v.channelId AND c.isDeleted = 0
                """.trimIndent()
        )

        sql.andIf(true, "v.isDeleted = 0")
        sql.andIf(true, "v.courseId = ?", courseId)

        sql.andIf(
            filters.searchQuery.isNotBlank(), """
                (v.title LIKE ? OR c.title LIKE ?)
                """.trimIndent(), "%${filters.searchQuery}%", "%${filters.searchQuery}%"
        )

        if (filters.selectedChannels.isNotEmpty()) {
            val placeholders = filters.selectedChannels.joinToString(",") { "?" }
            sql.andIf(
                true, "v.channelId IN ($placeholders)", *filters.selectedChannels.toTypedArray()
            )
        }

        if (filters.hasLabelFilter()) {
            val labelConditions = buildList {
                if (filters.selectedLabels.isNotEmpty()) {
                    val placeholders = filters.selectedLabels.joinToString(",") { "?" }
                    add(
                        """
                        EXISTS (
                            SELECT 1 FROM video_label_cross_ref 
                            WHERE videoId = v.id 
                            AND labelId IN ($placeholders)
                        )
                        """.trimIndent()
                    )
                }

                if (filters.unassignedLabelSelected) {
                    add(
                        """
                        NOT EXISTS (
                            SELECT 1 FROM video_label_cross_ref 
                            WHERE videoId = v.id
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
                    add("v.speakersNationality IN ($placeholders)".trimIndent())
                }
                if (filters.unassignedCountrySelected) {
                    add("v.speakersNationality IS NULL")
                }
            }

            sql.andIf(
                condition = countryConditions.isNotEmpty(), sql = countryConditions.joinToString(
                    separator = " OR ", prefix = "(", postfix = ")"
                ), bindArgs = filters.selectedCountries.toTypedArray()
            )
        }

        when (sort) {
            WATCH_DATE_DESC -> {
                sql.orderBy("DATE(v.watchedOn / 1000, 'unixepoch') DESC")
                sql.orderBy("lower(v.title) ASC")
            }

            WATCH_DATE_ASC -> {
                sql.orderBy("DATE(v.watchedOn / 1000, 'unixepoch') ASC")
                sql.orderBy("lower(v.title) ASC")
            }

            TITLE_DESC -> {
                sql.orderBy("lower(v.title) DESC")
            }

            TITLE_ASC -> {
                sql.orderBy("lower(v.title) ASC")
            }

            CHANNEL_TITLE_DESC -> {
                sql.orderBy("lower(c.title) DESC")
                sql.orderBy("lower(v.title) ASC")
            }

            CHANNEL_TITLE_ASC -> {
                sql.orderBy("lower(c.title) ASC")
                sql.orderBy("lower(v.title) ASC")
            }

            else -> {
                sql.orderBy("DATE(v.watchedOn / 1000, 'unixepoch') DESC")
                sql.orderBy("lower(v.title) ASC")
            }
        }

        return sql.build()
    }
}