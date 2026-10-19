package com.example.model

data class RiskConfig(
    val maxPositionSizePct: Double = 25.0,  // Max % of total portfolio in one symbol
    val maxPortfolioExposurePct: Double = 80.0, // Max % of portfolio invested (leaving 20% cash)
    val defaultStopLossPct: Double = 3.0,   // Stop loss %
    val defaultTakeProfitPct: Double = 6.0  // Take profit %
)

data class RiskEvaluationResult(
    val isAllowed: Boolean,
    val adjustedQuantity: Double,
    val warningMessage: String? = null
)
