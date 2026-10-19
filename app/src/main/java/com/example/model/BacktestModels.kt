package com.example.model

data class BacktestConfig(
    val initialCapital: Double = 10000.0,
    val feePercent: Double = 0.1,       // 0.1% per trade
    val slippagePercent: Double = 0.05, // 0.05% slippage on price
    val positionSizePct: Double = 100.0, // Allocate % of available cash per trade
    val stopLossPct: Double? = null,    // Optional stop loss (e.g. 3.0%)
    val takeProfitPct: Double? = null,  // Optional take profit (e.g. 6.0%)
    val allowShortSelling: Boolean = false // Default long-only per requirements
)

data class TradeRecord(
    val id: String,
    val entryDate: String,
    val entryPrice: Double,
    val exitDate: String,
    val exitPrice: Double,
    val quantity: Double,
    val isLong: Boolean = true,
    val pnlDollar: Double,
    val pnlPercent: Double,
    val exitReason: String, // e.g. "Signal", "Stop Loss", "Take Profit", "End of Data"
    val durationBars: Int
)

data class EquityPoint(
    val date: String,
    val equity: Double,
    val cash: Double,
    val drawdownPct: Double,
    val benchmarkEquity: Double // Buy & Hold equity comparison
)

data class PerformanceMetrics(
    val initialCapital: Double,
    val finalCapital: Double,
    val totalReturnPct: Double,
    val buyAndHoldReturnPct: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRatePct: Double,
    val profitFactor: Double,
    val maxDrawdownPct: Double,
    val sharpeRatio: Double,
    val averageTradePnlPct: Double,
    val maxConsecutiveWins: Int,
    val maxConsecutiveLosses: Int
)

data class BacktestResult(
    val id: String,
    val datasetName: String,
    val symbol: String,
    val strategyName: String,
    val config: BacktestConfig,
    val strategyConfig: StrategyConfig,
    val metrics: PerformanceMetrics,
    val trades: List<TradeRecord>,
    val equityCurve: List<EquityPoint>,
    val executionTimestamp: Long = System.currentTimeMillis()
)
