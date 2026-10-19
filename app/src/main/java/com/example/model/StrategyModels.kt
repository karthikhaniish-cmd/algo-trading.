package com.example.model

enum class StrategyType(val displayName: String, val shortDesc: String) {
    SMA_CROSSOVER(
        "SMA Crossover",
        "Generates Buy when fast SMA crosses above slow SMA; Sell when fast crosses below."
    ),
    EMA_CROSSOVER(
        "EMA Crossover",
        "Exponential moving average crossover with higher sensitivity to recent price movements."
    ),
    RSI_MEAN_REVERSION(
        "RSI Strategy",
        "Generates Buy when RSI is below oversold threshold; Sell when above overbought threshold."
    )
}

enum class SignalType {
    BUY,
    SELL,
    HOLD
}

data class StrategyConfig(
    val strategyType: StrategyType = StrategyType.SMA_CROSSOVER,
    val fastPeriod: Int = 9,
    val slowPeriod: Int = 21,
    val rsiPeriod: Int = 14,
    val rsiOversold: Double = 30.0,
    val rsiOverbought: Double = 70.0
)

data class TradingSignal(
    val index: Int,
    val date: String,
    val price: Double,
    val signal: SignalType,
    val indicator1Value: Double?, // e.g. Fast SMA/EMA or RSI
    val indicator2Value: Double?, // e.g. Slow SMA/EMA or null
    val reason: String
)

data class IndicatorSeries(
    val dates: List<String>,
    val fastValues: List<Double?>,
    val slowValues: List<Double?>,
    val rsiValues: List<Double?>,
    val signals: List<TradingSignal>
)
