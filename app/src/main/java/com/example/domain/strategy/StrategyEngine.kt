package com.example.domain.strategy

import com.example.domain.indicators.TechnicalIndicators
import com.example.model.IndicatorSeries
import com.example.model.PriceBar
import com.example.model.SignalType
import com.example.model.StrategyConfig
import com.example.model.StrategyType
import com.example.model.TradingSignal
import java.util.Locale

object StrategyEngine {

    fun generateSeriesAndSignals(
        bars: List<PriceBar>,
        config: StrategyConfig
    ): IndicatorSeries {
        if (bars.isEmpty()) {
            return IndicatorSeries(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }

        val closes = bars.map { it.close }
        val dates = bars.map { it.date }

        return when (config.strategyType) {
            StrategyType.SMA_CROSSOVER -> {
                val fastSma = TechnicalIndicators.calculateSMA(closes, config.fastPeriod)
                val slowSma = TechnicalIndicators.calculateSMA(closes, config.slowPeriod)
                val signals = generateCrossoverSignals(
                    bars = bars,
                    fast = fastSma,
                    slow = slowSma,
                    fastLabel = "SMA(${config.fastPeriod})",
                    slowLabel = "SMA(${config.slowPeriod})"
                )
                IndicatorSeries(
                    dates = dates,
                    fastValues = fastSma,
                    slowValues = slowSma,
                    rsiValues = List(bars.size) { null },
                    signals = signals
                )
            }
            StrategyType.EMA_CROSSOVER -> {
                val fastEma = TechnicalIndicators.calculateEMA(closes, config.fastPeriod)
                val slowEma = TechnicalIndicators.calculateEMA(closes, config.slowPeriod)
                val signals = generateCrossoverSignals(
                    bars = bars,
                    fast = fastEma,
                    slow = slowEma,
                    fastLabel = "EMA(${config.fastPeriod})",
                    slowLabel = "EMA(${config.slowPeriod})"
                )
                IndicatorSeries(
                    dates = dates,
                    fastValues = fastEma,
                    slowValues = slowEma,
                    rsiValues = List(bars.size) { null },
                    signals = signals
                )
            }
            StrategyType.RSI_MEAN_REVERSION -> {
                val rsi = TechnicalIndicators.calculateRSI(closes, config.rsiPeriod)
                val signals = generateRsiSignals(
                    bars = bars,
                    rsiValues = rsi,
                    oversold = config.rsiOversold,
                    overbought = config.rsiOverbought,
                    period = config.rsiPeriod
                )
                IndicatorSeries(
                    dates = dates,
                    fastValues = List(bars.size) { null },
                    slowValues = List(bars.size) { null },
                    rsiValues = rsi,
                    signals = signals
                )
            }
        }
    }

    private fun generateCrossoverSignals(
        bars: List<PriceBar>,
        fast: List<Double?>,
        slow: List<Double?>,
        fastLabel: String,
        slowLabel: String
    ): List<TradingSignal> {
        val signals = ArrayList<TradingSignal>(bars.size)

        for (i in bars.indices) {
            val bar = bars[i]
            val currFast = fast[i]
            val currSlow = slow[i]

            if (currFast == null || currSlow == null || i == 0) {
                signals.add(
                    TradingSignal(
                        index = i,
                        date = bar.date,
                        price = bar.close,
                        signal = SignalType.HOLD,
                        indicator1Value = currFast,
                        indicator2Value = currSlow,
                        reason = "Warming up: insufficient bars for $fastLabel & $slowLabel."
                    )
                )
                continue
            }

            val prevFast = fast[i - 1]
            val prevSlow = slow[i - 1]

            if (prevFast == null || prevSlow == null) {
                signals.add(
                    TradingSignal(
                        index = i,
                        date = bar.date,
                        price = bar.close,
                        signal = SignalType.HOLD,
                        indicator1Value = currFast,
                        indicator2Value = currSlow,
                        reason = "Warm-up phase completed."
                    )
                )
                continue
            }

            // Golden Cross: fast crosses above slow
            val isBullishCross = prevFast <= prevSlow && currFast > currSlow
            // Death Cross: fast crosses below slow
            val isBearishCross = prevFast >= prevSlow && currFast < currSlow

            val (sigType, reason) = when {
                isBullishCross -> {
                    SignalType.BUY to String.format(
                        Locale.US,
                        "Golden Cross: %s (%.2f) crossed above %s (%.2f) confirming bullish momentum.",
                        fastLabel, currFast, slowLabel, currSlow
                    )
                }
                isBearishCross -> {
                    SignalType.SELL to String.format(
                        Locale.US,
                        "Death Cross: %s (%.2f) crossed below %s (%.2f) confirming bearish reversal.",
                        fastLabel, currFast, slowLabel, currSlow
                    )
                }
                currFast > currSlow -> {
                    SignalType.HOLD to String.format(
                        Locale.US,
                        "Holding Long: %s (%.2f) remains above %s (%.2f).",
                        fastLabel, currFast, slowLabel, currSlow
                    )
                }
                else -> {
                    SignalType.HOLD to String.format(
                        Locale.US,
                        "Holding Neutral/Short: %s (%.2f) is below %s (%.2f).",
                        fastLabel, currFast, slowLabel, currSlow
                    )
                }
            }

            signals.add(
                TradingSignal(
                    index = i,
                    date = bar.date,
                    price = bar.close,
                    signal = sigType,
                    indicator1Value = currFast,
                    indicator2Value = currSlow,
                    reason = reason
                )
            )
        }
        return signals
    }

    private fun generateRsiSignals(
        bars: List<PriceBar>,
        rsiValues: List<Double?>,
        oversold: Double,
        overbought: Double,
        period: Int
    ): List<TradingSignal> {
        val signals = ArrayList<TradingSignal>(bars.size)

        for (i in bars.indices) {
            val bar = bars[i]
            val currRsi = rsiValues[i]

            if (currRsi == null || i == 0) {
                signals.add(
                    TradingSignal(
                        index = i,
                        date = bar.date,
                        price = bar.close,
                        signal = SignalType.HOLD,
                        indicator1Value = currRsi,
                        indicator2Value = null,
                        reason = "Warming up: RSI($period) requires $period bars."
                    )
                )
                continue
            }

            val prevRsi = rsiValues[i - 1] ?: currRsi

            // Oversold bounce: crosses back above oversold OR currently deep oversold
            val isOversoldSignal = (prevRsi <= oversold && currRsi > oversold) || (currRsi < oversold && i % 5 == 0)
            // Overbought breakdown: crosses below overbought OR currently deep overbought
            val isOverboughtSignal = (prevRsi >= overbought && currRsi < overbought) || (currRsi > overbought && i % 5 == 0)

            val (sigType, reason) = when {
                isOversoldSignal -> {
                    SignalType.BUY to String.format(
                        Locale.US,
                        "Oversold Rebound: RSI(%.1f) triggered Buy below threshold %.1f (mean reversion anticipated).",
                        currRsi, oversold
                    )
                }
                isOverboughtSignal -> {
                    SignalType.SELL to String.format(
                        Locale.US,
                        "Overbought Exit: RSI(%.1f) triggered Sell above threshold %.1f (price extended).",
                        currRsi, overbought
                    )
                }
                currRsi < oversold -> {
                    SignalType.BUY to String.format(
                        Locale.US,
                        "RSI(%.1f) in extreme oversold territory (< %.1f).",
                        currRsi, oversold
                    )
                }
                currRsi > overbought -> {
                    SignalType.SELL to String.format(
                        Locale.US,
                        "RSI(%.1f) in extreme overbought territory (> %.1f).",
                        currRsi, overbought
                    )
                }
                else -> {
                    SignalType.HOLD to String.format(
                        Locale.US,
                        "Neutral RSI (%.1f): within normal oscillation bounds (%.1f - %.1f).",
                        currRsi, oversold, overbought
                    )
                }
            }

            signals.add(
                TradingSignal(
                    index = i,
                    date = bar.date,
                    price = bar.close,
                    signal = sigType,
                    indicator1Value = currRsi,
                    indicator2Value = null,
                    reason = reason
                )
            )
        }
        return signals
    }
}
