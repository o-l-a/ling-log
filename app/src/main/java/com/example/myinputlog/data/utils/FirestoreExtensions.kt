package com.example.myinputlog.data.utils

import kotlinx.serialization.json.*

/**
 * Converts a KotlinX JsonObject into a Kotlin MutableMap suitable for Firestore.
 * Filters out null values.
 */
fun JsonObject.toFirestoreMap(): MutableMap<String, Any> {
    return this.filterValues { it !is JsonNull }
        .mapValues { (_, value) -> value.unwrap() }
        .toMutableMap()
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