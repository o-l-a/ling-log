package com.example.myinputlog.data.utils

import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.reflect.full.memberProperties

/**
 * Converts a Data Class to a Map that Firestore understands.
 * Preserves Date types, respects Firestore annotations, and allows exclusions.
 */
fun Any.toFirestoreMap(excludeFields: Set<String> = emptySet()): Map<String, Any> {
    return this::class.memberProperties.filter { it.name !in excludeFields }.associate { prop ->
            val value = prop.getter.call(this)
            val finalValue = if (prop.annotations.any { it is ServerTimestamp }) {
                com.google.firebase.firestore.FieldValue.serverTimestamp()
            } else {
                value
            }
            prop.name to finalValue
        }.filterValues { it != null }.mapValues { it.value!! }
}

/**
 * Recursively unwraps KotlinX Json types into standard Kotlin types.
 */
private fun JsonElement.unwrap(): Any {
    return when (this) {
        is JsonPrimitive -> {
            if (this.isString) this.content
            else this.booleanOrNull ?: this.longOrNull ?: this.doubleOrNull ?: this.content
        }

        is JsonArray -> this.map { it.unwrap() }
        is JsonObject -> this.toFirestoreMap()
        JsonNull -> throw IllegalStateException("JsonNull should be filtered out")
    }
}