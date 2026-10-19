package com.example.data.sample

import com.example.model.DatasetType
import com.example.model.PriceBar
import com.example.model.StockDataset
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.math.sqrt

object SampleDataGenerator {

    fun generateSyntheticDataset(
        id: String,
        symbol: String,
        name: String,
        description: String,
        startPrice: Double,
        dailyVolatility: Double,
        annualDrift: Double,
        numBars: Int = 120,
        seed: Long = 42L
    ): StockDataset {
        val random = Random(seed)
        val bars = ArrayList<PriceBar>(numBars)

        val calendar = Calendar.getInstance()
        calendar.time = Date()
        // Go back numBars business days
        calendar.add(Calendar.DAY_OF_YEAR, -((numBars * 7) / 5))

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var currentClose = startPrice
        val dt = 1.0 / 252.0 // 1 trading day

        var barsGenerated = 0
        while (barsGenerated < numBars) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                val dateStr = dateFormat.format(calendar.time)

                // Geometric Brownian Motion step: S(t+dt) = S(t) * exp((mu - 0.5 * sigma^2)*dt + sigma*sqrt(dt)*Z)
                val z = random.nextGaussian()
                val returnStep = (annualDrift - 0.5 * dailyVolatility * dailyVolatility) * dt +
                        (dailyVolatility * sqrt(dt) * z)
                val nextClose = max(1.0, currentClose * exp(returnStep))

                val intradayVolatility = dailyVolatility * 0.4
                val openDrift = (random.nextDouble() - 0.5) * intradayVolatility * currentClose
                val openPrice = max(1.0, currentClose + openDrift)

                val upperMax = max(openPrice, nextClose)
                val lowerMin = min(openPrice, nextClose)

                val highWick = random.nextDouble() * intradayVolatility * currentClose
                val lowWick = random.nextDouble() * intradayVolatility * currentClose

                val highPrice = upperMax + highWick
                val lowPrice = max(0.5, lowerMin - lowWick)

                // Volume correlated with price movement
                val baseVolume = 1_500_000L
                val volMultiplier = 0.6 + random.nextDouble() * 0.8 + (kotlin.math.abs(z) * 0.5)
                val volume = (baseVolume * volMultiplier).roundToLong()

                bars.add(
                    PriceBar(
                        date = dateStr,
                        open = roundTwoDecimals(openPrice),
                        high = roundTwoDecimals(highPrice),
                        low = roundTwoDecimals(lowPrice),
                        close = roundTwoDecimals(nextClose),
                        volume = volume
                    )
                )

                currentClose = nextClose
                barsGenerated++
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return StockDataset(
            id = id,
            symbol = symbol,
            name = name,
            type = DatasetType.SYNTHETIC,
            description = description,
            bars = bars
        )
    }

    fun getInitialDemoDatasets(): List<StockDataset> {
        return listOf(
            generateSyntheticDataset(
                id = "demo_aapl",
                symbol = "AAPL",
                name = "Apple Inc. (Synthetic)",
                description = "Synthetic tech growth equity with upward momentum and periodic pullbacks.",
                startPrice = 175.0,
                dailyVolatility = 0.22,
                annualDrift = 0.18,
                numBars = 150,
                seed = 101L
            ),
            generateSyntheticDataset(
                id = "demo_tsla",
                symbol = "TSLA",
                name = "Tesla Inc. (Synthetic)",
                description = "High beta volatile asset with deep oversold oscillations, ideal for RSI testing.",
                startPrice = 210.0,
                dailyVolatility = 0.45,
                annualDrift = 0.08,
                numBars = 150,
                seed = 202L
            ),
            generateSyntheticDataset(
                id = "demo_spy",
                symbol = "SPY",
                name = "S&P 500 ETF (Synthetic)",
                description = "Broad market benchmark showing macroeconomic trends and moving average golden crosses.",
                startPrice = 490.0,
                dailyVolatility = 0.14,
                annualDrift = 0.12,
                numBars = 150,
                seed = 303L
            ),
            generateSyntheticDataset(
                id = "demo_btc",
                symbol = "BTC-USD",
                name = "Bitcoin (Synthetic)",
                description = "Crypto asset simulation exhibiting explosive breakouts and sharp trend reversals.",
                startPrice = 58000.0,
                dailyVolatility = 0.65,
                annualDrift = 0.35,
                numBars = 150,
                seed = 404L
            )
        )
    }

    fun generateSampleCsvContent(bars: List<PriceBar>): String {
        val sb = StringBuilder()
        sb.append("Date,Open,High,Low,Close,Volume\n")
        for (bar in bars) {
            sb.append(String.format(Locale.US, "%s,%.2f,%.2f,%.2f,%.2f,%d\n",
                bar.date, bar.open, bar.high, bar.low, bar.close, bar.volume))
        }
        return sb.toString()
    }

    private fun roundTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToLong() / 100.0
    }
}
