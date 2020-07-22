package com.justai.jaicf.profit

import com.justai.jaicf.profit.model.Profit
import com.justai.jaicf.profit.model.Product
import javax.measure.Unit

import tec.units.ri.unit.Units.*
import tec.units.ri.unit.MetricPrefix.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object ProfitCalculator {

    private val units = mapOf<String, Unit<*>>(
        "g" to GRAM,
        "mg" to MILLI(GRAM),
        "kg" to KILO(GRAM),
        "m" to METRE,
        "sm" to CENTI(METRE),
        "mm" to MILLI(METRE),
        "l" to LITRE,
        "ml" to MILLI(LITRE),
        "sec" to SECOND,
        "min" to MINUTE,
        "hour" to HOUR
    )

    fun calculateProfit(first: Product, second: Product): Profit? {
        val firstUnit = units[first.unit]
        val secondUnit = units[second.unit]
        var converted: Number = first.amount

        if (firstUnit != null && secondUnit != null) {
            val converter = firstUnit.getConverterToAny(secondUnit)
            converted = converter.convert(first.amount)
        }

        val firstPrice = first.price / converted.toDouble()
        val secondPrice = second.price / second.amount

        return if (firstPrice == secondPrice) {
            null
        } else {
            val bestPrice = min(firstPrice, secondPrice)
            val worsePrice = max(firstPrice, secondPrice)
            val stuff = if (firstPrice < secondPrice) first else second
            val percent = round((worsePrice - bestPrice) / bestPrice  * 100).toInt()
            Profit(percent, stuff)
        }
    }
}