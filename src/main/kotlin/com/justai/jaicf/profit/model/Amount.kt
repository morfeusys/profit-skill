package com.justai.jaicf.profit.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.content

fun JsonElement.toAmount() = when {
    content == "пол" -> 0.5
    content.startsWith("полтор") -> 1.5
    content.startsWith("десят") -> 10.0
    content.startsWith("дюжин") -> 12.0
    else -> content.toDoubleOrNull()
}