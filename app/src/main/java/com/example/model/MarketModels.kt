package com.example.model

enum class DatasetType {
    SYNTHETIC,
    HISTORICAL,
    CUSTOM_CSV
}

data class PriceBar(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    val change: Double get() = close - open
    val changePercent: Double get() = if (open != 0.0) ((close - open) / open) * 100.0 else 0.0
    val isBullish: Boolean get() = close >= open
}

data class StockDataset(
    val id: String,
    val symbol: String,
    val name: String,
    val type: DatasetType,
    val description: String,
    val bars: List<PriceBar>,
    val createdAt: Long = System.currentTimeMillis()
) {
    val startDate: String get() = bars.firstOrNull()?.date ?: ""
    val endDate: String get() = bars.lastOrNull()?.date ?: ""
    val barCount: Int get() = bars.size
    val latestBar: PriceBar? get() = bars.lastOrNull()
}

data class CsvValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val validBars: List<PriceBar>,
    val parsedCount: Int = validBars.size,
    val skippedCount: Int = 0
)
