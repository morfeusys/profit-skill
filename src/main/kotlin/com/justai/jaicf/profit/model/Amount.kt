package com.justai.jaicf.profit.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.content

fun JsonElement.toAmount() = content.replace('o', '.').toDouble()