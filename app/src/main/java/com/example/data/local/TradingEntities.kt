package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "datasets")
data class DatasetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val type: String, // SYNTHETIC, HISTORICAL, CUSTOM_CSV
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "price_bars",
    indices = [Index(value = ["datasetId", "date"], unique = true)]
)
data class PriceBarEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val datasetId: String,
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

@Entity(tableName = "paper_orders")
data class PaperOrderEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val symbol: String,
    val side: String, // BUY, SELL
    val quantity: Double,
    val price: Double,
    val strategy: String,
    val status: String,
    val realizedPnl: Double = 0.0,
    val note: String = ""
)

@Entity(tableName = "paper_positions")
data class PaperPositionEntity(
    @PrimaryKey val symbol: String,
    val quantity: Double,
    val averagePrice: Double,
    val currentPrice: Double
)

@Entity(tableName = "paper_account")
data class PaperAccountEntity(
    @PrimaryKey val id: Int = 1,
    val startingCash: Double = 10000.0,
    val cash: Double = 10000.0,
    val realizedPnl: Double = 0.0
)

@Entity(tableName = "backtest_records")
data class BacktestRecordEntity(
    @PrimaryKey val id: String,
    val datasetName: String,
    val symbol: String,
    val strategyName: String,
    val initialCapital: Double,
    val finalCapital: Double,
    val totalReturnPct: Double,
    val buyAndHoldReturnPct: Double,
    val winRatePct: Double,
    val totalTrades: Int,
    val maxDrawdownPct: Double,
    val sharpeRatio: Double,
    val tradesSummaryJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
