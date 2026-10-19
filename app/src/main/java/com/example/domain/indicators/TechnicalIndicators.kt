package com.example.domain.indicators

import kotlin.math.max

object TechnicalIndicators {

    /**
     * Calculates Simple Moving Average (SMA) over a list of prices.
     * Returns null for indices where insufficient historical bars exist (< period - 1).
     */
    fun calculateSMA(prices: List<Double>, period: Int): List<Double?> {
        if (period <= 0 || prices.isEmpty()) return List(prices.size) { null }
        val result = ArrayList<Double?>(prices.size)
        var rollingSum = 0.0

        for (i in prices.indices) {
            rollingSum += prices[i]
            if (i >= period) {
                rollingSum -= prices[i - period]
            }
            if (i >= period - 1) {
                result.add(rollingSum / period)
            } else {
                result.add(null)
            }
        }
        return result
    }

    /**
     * Calculates Exponential Moving Average (EMA).
     * Multiplier alpha = 2 / (period + 1).
     * First value after period - 1 is initialized using the SMA of first `period` bars.
     */
    fun calculateEMA(prices: List<Double>, period: Int): List<Double?> {
        if (period <= 0 || prices.isEmpty()) return List(prices.size) { null }
        val result = ArrayList<Double?>(prices.size)
        val alpha = 2.0 / (period + 1.0)
        var previousEma: Double? = null

        var initialSum = 0.0
        for (i in prices.indices) {
            if (i < period) {
                initialSum += prices[i]
                if (i == period - 1) {
                    val initialSma = initialSum / period
                    previousEma = initialSma
                    result.add(initialSma)
                } else {
                    result.add(null)
                }
            } else {
                val currentEma = (prices[i] * alpha) + (previousEma!! * (1.0 - alpha))
                previousEma = currentEma
                result.add(currentEma)
            }
        }
        return result
    }

    /**
     * Calculates Relative Strength Index (RSI) using Wilder's smoothed moving average.
     * Standard 14-period RSI ranges from 0 to 100.
     */
    fun calculateRSI(prices: List<Double>, period: Int = 14): List<Double?> {
        if (period <= 0 || prices.size <= period) return List(prices.size) { null }
        val result = ArrayList<Double?>(prices.size)

        var avgGain = 0.0
        var avgLoss = 0.0

        for (i in prices.indices) {
            if (i == 0) {
                result.add(null)
                continue
            }

            val change = prices[i] - prices[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0

            if (i < period) {
                avgGain += gain
                avgLoss += loss
                result.add(null)
            } else if (i == period) {
                avgGain = (avgGain + gain) / period
                avgLoss = (avgLoss + loss) / period

                val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
                val rsi = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + rs))
                result.add(rsi.coerceIn(0.0, 100.0))
            } else {
                avgGain = ((avgGain * (period - 1)) + gain) / period
                avgLoss = ((avgLoss * (period - 1)) + loss) / period

                val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
                val rsi = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + rs))
                result.add(rsi.coerceIn(0.0, 100.0))
            }
        }
        return result
    }
}
