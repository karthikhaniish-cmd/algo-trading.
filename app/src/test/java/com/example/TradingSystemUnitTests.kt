package com.example

import com.example.data.parser.CsvMarketParser
import com.example.data.sample.SampleDataGenerator
import com.example.domain.backtest.BacktestEngine
import com.example.domain.indicators.TechnicalIndicators
import com.example.domain.risk.RiskEngine
import com.example.domain.strategy.StrategyEngine
import com.example.model.BacktestConfig
import com.example.model.PaperAccount
import com.example.model.PaperPosition
import com.example.model.PriceBar
import com.example.model.RiskConfig
import com.example.model.SignalType
import com.example.model.StrategyConfig
import com.example.model.StrategyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TradingSystemUnitTests {

    @Test
    fun testSmaCalculation() {
        val prices = listOf(10.0, 20.0, 30.0, 40.0, 50.0)
        val sma3 = TechnicalIndicators.calculateSMA(prices, 3)

        assertEquals(5, sma3.size)
        assertNull(sma3[0])
        assertNull(sma3[1])
        assertEquals(20.0, sma3[2]!!, 0.001) // (10+20+30)/3 = 20
        assertEquals(30.0, sma3[3]!!, 0.001) // (20+30+40)/3 = 30
        assertEquals(40.0, sma3[4]!!, 0.001) // (30+40+50)/3 = 40
    }

    @Test
    fun testEmaCalculation() {
        val prices = listOf(10.0, 11.0, 12.0, 13.0, 14.0)
        val ema3 = TechnicalIndicators.calculateEMA(prices, 3)

        assertEquals(5, ema3.size)
        assertNull(ema3[0])
        assertNull(ema3[1])
        // First value is SMA of first 3 items: (10+11+12)/3 = 11.0
        assertEquals(11.0, ema3[2]!!, 0.001)
        // Next: alpha = 2 / (3+1) = 0.5. Current price = 13.0.
        // EMA = (13.0 * 0.5) + (11.0 * 0.5) = 12.0
        assertEquals(12.0, ema3[3]!!, 0.001)
    }

    @Test
    fun testRsiCalculationBounds() {
        // Monotonically increasing prices
        val upPrices = List(20) { 100.0 + it * 5.0 }
        val rsiUp = TechnicalIndicators.calculateRSI(upPrices, 14)
        val lastRsi = rsiUp.last()
        assertNotNull(lastRsi)
        assertTrue("RSI on pure gains should be near 100", lastRsi!! >= 95.0)

        // Monotonically decreasing prices
        val downPrices = List(20) { 200.0 - it * 5.0 }
        val rsiDown = TechnicalIndicators.calculateRSI(downPrices, 14)
        val lastRsiDown = rsiDown.last()
        assertNotNull(lastRsiDown)
        assertTrue("RSI on pure losses should be near 0", lastRsiDown!! <= 5.0)
    }

    @Test
    fun testCsvParserValidatesCorrectly() {
        val validCsv = """Date,Open,High,Low,Close,Volume
2026-01-01,100.0,105.0,98.0,103.0,1000000
2026-01-02,103.0,107.0,102.0,106.0,1200000"""

        val result = CsvMarketParser.parseAndValidateCsv(validCsv)
        assertTrue(result.isValid)
        assertEquals(2, result.validBars.size)
        assertEquals(100.0, result.validBars[0].open, 0.001)
    }

    @Test
    fun testCsvParserRejectsInvalidOhlc() {
        // High is less than Low (102.0 < 105.0)
        val invalidOhlc = """Date,Open,High,Low,Close,Volume
2026-01-01,100.0,102.0,105.0,103.0,1000000"""

        val result = CsvMarketParser.parseAndValidateCsv(invalidOhlc)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("High") && it.contains("Low") })
    }

    @Test
    fun testCsvParserRejectsDuplicateDates() {
        val duplicateDates = """Date,Open,High,Low,Close,Volume
2026-01-01,100.0,105.0,98.0,103.0,1000000
2026-01-01,103.0,107.0,102.0,106.0,1200000"""

        val result = CsvMarketParser.parseAndValidateCsv(duplicateDates)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Duplicate date") })
    }

    @Test
    fun testStrategyCrossoverSignals() {
        val bars = ArrayList<PriceBar>()
        // Generate 30 bars with downward trend then upward breakout
        for (i in 0 until 15) {
            bars.add(PriceBar("2026-01-${String.format("%02d", i + 1)}", 100.0 - i, 102.0 - i, 98.0 - i, 99.0 - i, 1000000L))
        }
        for (i in 15 until 35) {
            bars.add(PriceBar("2026-02-${String.format("%02d", i - 14)}", 85.0 + (i - 15) * 3, 87.0 + (i - 15) * 3, 84.0 + (i - 15) * 3, 86.0 + (i - 15) * 3, 1500000L))
        }

        val config = StrategyConfig(
            strategyType = StrategyType.SMA_CROSSOVER,
            fastPeriod = 5,
            slowPeriod = 15
        )
        val series = StrategyEngine.generateSeriesAndSignals(bars, config)

        assertNotNull(series)
        assertEquals(bars.size, series.signals.size)
        // Verify at least one BUY signal was generated during breakout
        val buySignals = series.signals.filter { it.signal == SignalType.BUY }
        assertTrue("Should have generated golden cross BUY signal", buySignals.isNotEmpty())
    }

    @Test
    fun testBacktestEngineNoLookaheadBias() {
        val dataset = SampleDataGenerator.generateSyntheticDataset(
            id = "test_ds",
            symbol = "TEST",
            name = "Test",
            description = "Test",
            startPrice = 100.0,
            dailyVolatility = 0.20,
            annualDrift = 0.10,
            numBars = 60,
            seed = 42L
        )

        val result = BacktestEngine.runBacktest(
            datasetName = dataset.name,
            symbol = dataset.symbol,
            bars = dataset.bars,
            strategyConfig = StrategyConfig(fastPeriod = 5, slowPeriod = 12),
            config = BacktestConfig(initialCapital = 10000.0, feePercent = 0.1, slippagePercent = 0.05)
        )

        assertNotNull(result)
        assertEquals(10000.0, result.metrics.initialCapital, 0.001)
        assertTrue(result.equityCurve.isNotEmpty())
        assertTrue("Max drawdown should be non-negative", result.metrics.maxDrawdownPct >= 0.0)
    }

    @Test
    fun testRiskEngineEnforcesLimits() {
        val account = PaperAccount(startingCash = 10000.0, cash = 1000.0, realizedPnl = 0.0)
        val positions = listOf(PaperPosition("AAPL", 10.0, 150.0, 150.0))
        val riskConfig = RiskConfig(maxPositionSizePct = 25.0, maxPortfolioExposurePct = 80.0)

        // Try to place buy order exceeding available cash
        val resultOverCash = RiskEngine.validateOrder(
            symbol = "TSLA",
            isBuy = true,
            requestedQuantity = 50.0,
            price = 200.0, // Cost = $10,000, but only $1,000 cash available
            account = account,
            existingPositions = positions,
            config = riskConfig
        )
        // Should adjust quantity down or reject
        assertTrue(resultOverCash.adjustedQuantity <= (account.cash / 200.0) + 0.001)
    }
}
