package com.example.myinputlog.data.local.query

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

class QueryBuilder(private val baseQuery: String) {
    private val conditions = mutableListOf<String>()
    private val groupings = mutableListOf<String>()
    private val orderings = mutableListOf<String>()
    private val args = mutableListOf<Any>()

    fun andIf(condition: Boolean, sql: String, vararg bindArgs: Any) {
        if (condition) {
            conditions.add(sql)
            args.addAll(bindArgs)
        }
    }

    fun groupBy(statement: String) {
        groupings.add(statement)
    }

    fun orderBy(statement: String) {
        orderings.add(statement)
    }

    fun build(): SupportSQLiteQuery {
        val finalSql = buildString {
            append(baseQuery)
            if (conditions.isNotEmpty()) {
                append(" WHERE ")
                append(conditions.joinToString(" AND "))
            }
            if (groupings.isNotEmpty()) {
                append(" GROUP BY ")
                append(groupings.joinToString(", "))
            }
            if (orderings.isNotEmpty()) {
                append(" ORDER BY ")
                append(orderings.joinToString(", "))
            }

        }
        Log.d(TAG, finalSql)
        Log.d(TAG, args.toString())
        return SimpleSQLiteQuery(finalSql, args.toTypedArray())
    }

    companion object {
        private const val TAG = "QueryBuilder"
    }
}