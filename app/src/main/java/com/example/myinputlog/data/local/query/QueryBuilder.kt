package com.example.myinputlog.data.local.query

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

class QueryBuilder(private val baseQuery: String) {
    private val conditions = mutableListOf<String>()
    private val args = mutableListOf<Any>()

    fun andIf(condition: Boolean, sql: String, vararg bindArgs: Any) {
        if (condition) {
            conditions.add(sql)
            args.addAll(bindArgs)
        }
    }

    fun build(orderBy: String): SupportSQLiteQuery {
        val finalSql = buildString {
            append(baseQuery)
            if (conditions.isNotEmpty()) {
                append(" WHERE ")
                append(conditions.joinToString(" AND "))
            }
            append(" ORDER BY $orderBy")
        }
        return SimpleSQLiteQuery(finalSql, args.toTypedArray())
    }
}