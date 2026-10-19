package com.example.model

enum class OrderSide {
    BUY,
    SELL
}

enum class OrderStatus {
    FILLED,
    REJECTED,
    PENDING
}

data class PaperOrder(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val symbol: String,
    val side: OrderSide,
    val quantity: Double,
    val price: Double,
    val strategy: String,
    val status: OrderStatus,
    val realizedPnl: Double = 0.0,
    val executionNote: String = ""
)

data class PaperPosition(
    val symbol: String,
    val quantity: Double,
    val averagePrice: Double,
    val currentPrice: Double
) {
    val totalCost: Double get() = quantity * averagePrice
    val marketValue: Double get() = quantity * currentPrice
    val unrealizedPnl: Double get() = marketValue - totalCost
    val unrealizedPnlPct: Double get() = if (totalCost != 0.0) (unrealizedPnl / totalCost) * 100.0 else 0.0
}

data class PaperAccount(
    val startingCash: Double = 10000.0,
    val cash: Double = 10000.0,
    val realizedPnl: Double = 0.0
)
