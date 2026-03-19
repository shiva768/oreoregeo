package com.zelretch.oreoregeo.util

fun parseAdditionalTags(
    name: String,
    category: String,
    categoryValue: String,
    additionalTags: String
): Map<String, String> {
    val tags = mutableMapOf(
        "name" to name,
        category to categoryValue
    )
    if (additionalTags.isNotBlank()) {
        additionalTags.split(",").forEach { pair ->
            val parts = pair.trim().split("=")
            if (parts.size == 2) {
                tags[parts[0].trim()] = parts[1].trim()
            }
        }
    }
    return tags
}
