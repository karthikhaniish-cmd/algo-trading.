package com.example.data.repository

import com.example.data.local.BacktestRecordEntity
import com.example.data.local.DatasetEntity
import com.example.data.local.PaperAccountEntity
import com.example.data.local.PaperOrderEntity
import com.example.data.local.PaperPositionEntity
import com.example.data.local.PriceBarEntity
import com.example.data.local.TradingDao
import com.example.data.parser.CsvMarketParser
import com.example.data.sample.SampleDataGenerator
import com.example.domain.risk.RiskEngine
import com.example.model.BacktestResult
import com.example.model.CsvValidationResult
import com.example.model.DatasetType
import com.example.model.OrderSide
import com.example.model.OrderStatus
import com.example.model.PaperAccount
import com.example.model.PaperOrder
import com.example.model.PaperPosition
import com.example.model.PriceBar
import com.example.model.RiskConfig
import com.example.model.StockDataset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class TradingRepository(private val dao: TradingDao) {

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val existing = dao.getAllDatasets().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val demos = SampleDataGenerator.getInitialDemoDatasets()
            for (demo in demos) {
                dao.insertDataset(
                    DatasetEntity(
                        id = demo.id,
                        name = demo.name,
                        symbol = demo.symbol,
                        type = demo.type.name,
                        description = demo.description,
                        createdAt = demo.createdAt
                    )
                )
                dao.insertBars(
                    demo.bars.map { bar ->
                        PriceBarEntity(
                            datasetId = demo.id,
                            date = bar.date,
                            open = bar.open,
                            high = bar.high,
                            low = bar.low,
                            close = bar.close,
                            volume = bar.volume
                        )
                    }
                )
            }
        }

        val existingAccount = dao.getPaperAccountSync()
        if (existingAccount == null) {
            dao.updatePaperAccount(
                PaperAccountEntity(id = 1, startingCash = 10000.0, cash = 10000.0, realizedPnl = 0.0)
            )
        }
    }

    fun getDatasets(): Flow<List<StockDataset>> {
        return dao.getAllDatasets().map { entities ->
            entities.map { entity ->
                // For list overview, bars can be loaded on demand or cached
                StockDataset(
                    id = entity.id,
                    name = entity.name,
                    symbol = entity.symbol,
                    type = try { DatasetType.valueOf(entity.type) } catch (_: Exception) { DatasetType.SYNTHETIC },
                    description = entity.description,
                    bars = emptyList(),
                    createdAt = entity.createdAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getFullDataset(datasetId: String): StockDataset? = withContext(Dispatchers.IO) {
        val entity = dao.getDatasetById(datasetId) ?: return@withContext null
        val barEntities = dao.getBarsForDatasetSync(datasetId)
        val bars = barEntities.map {
            PriceBar(
                date = it.date,
                open = it.open,
                high = it.high,
                low = it.low,
                close = it.close,
                volume = it.volume
            )
        }
        StockDataset(
            id = entity.id,
            name = entity.name,
            symbol = entity.symbol,
            type = try { DatasetType.valueOf(entity.type) } catch (_: Exception) { DatasetType.SYNTHETIC },
            description = entity.description,
            bars = bars,
            createdAt = entity.createdAt
        )
    }

    fun getBarsForDatasetFlow(datasetId: String): Flow<List<PriceBar>> {
        return dao.getBarsForDataset(datasetId).map { list ->
            list.map {
                PriceBar(
                    date = it.date,
                    open = it.open,
                    high = it.high,
                    low = it.low,
                    close = it.close,
                    volume = it.volume
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun importCsvDataset(
        symbol: String,
        name: String,
        description: String,
        csvContent: String
    ): CsvValidationResult = withContext(Dispatchers.IO) {
        val validation = CsvMarketParser.parseAndValidateCsv(csvContent)
        if (validation.isValid && validation.validBars.isNotEmpty()) {
            val datasetId = "custom_" + UUID.randomUUID().toString().take(8)
            val datasetEntity = DatasetEntity(
                id = datasetId,
                name = name.ifBlank { "$symbol Dataset" },
                symbol = symbol.uppercase().trim(),
                type = DatasetType.CUSTOM_CSV.name,
                description = description.ifBlank { "User imported CSV dataset with ${validation.validBars.size} bars." },
                createdAt = System.currentTimeMillis()
            )
            dao.insertDataset(datasetEntity)
            dao.insertBars(
                validation.validBars.map { bar ->
                    PriceBarEntity(
                        datasetId = datasetId,
                        date = bar.date,
                        open = bar.open,
                        high = bar.high,
                        low = bar.low,
                        close = bar.close,
                        volume = bar.volume
                    )
                }
            )
        }
        validation
    }

    suspend fun createSyntheticDataset(
        symbol: String,
        name: String,
        description: String,
        startPrice: Double,
        volatility: Double,
        drift: Double,
        barsCount: Int
    ): StockDataset = withContext(Dispatchers.IO) {
        val datasetId = "synth_" + UUID.randomUUID().toString().take(8)
        val dataset = SampleDataGenerator.generateSyntheticDataset(
            id = datasetId,
            symbol = symbol.uppercase().trim(),
            name = name,
            description = description,
            startPrice = startPrice,
            dailyVolatility = volatility,
            annualDrift = drift,
            numBars = barsCount,
            seed = System.currentTimeMillis()
        )
        dao.insertDataset(
            DatasetEntity(
                id = dataset.id,
                name = dataset.name,
                symbol = dataset.symbol,
                type = dataset.type.name,
                description = dataset.description,
                createdAt = dataset.createdAt
            )
        )
        dao.insertBars(
            dataset.bars.map { bar ->
                PriceBarEntity(
                    datasetId = dataset.id,
                    date = bar.date,
                    open = bar.open,
                    high = bar.high,
                    low = bar.low,
                    close = bar.close,
                    volume = bar.volume
                )
            }
        )
        dataset
    }

    suspend fun deleteDataset(datasetId: String) = withContext(Dispatchers.IO) {
        dao.deleteBarsForDataset(datasetId)
        dao.deleteDataset(datasetId)
    }

    // Paper Trading Functions
    fun getPaperAccountFlow(): Flow<PaperAccount> {
        return dao.getPaperAccount().map { entity ->
            if (entity != null) {
                PaperAccount(
                    startingCash = entity.startingCash,
                    cash = entity.cash,
                    realizedPnl = entity.realizedPnl
                )
            } else {
                PaperAccount()
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPositionsFlow(): Flow<List<PaperPosition>> {
        return dao.getAllPositions().map { list ->
            list.map {
                PaperPosition(
                    symbol = it.symbol,
                    quantity = it.quantity,
                    averagePrice = it.averagePrice,
                    currentPrice = it.currentPrice
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getOrdersFlow(): Flow<List<PaperOrder>> {
        return dao.getAllPaperOrders().map { list ->
            list.map {
                PaperOrder(
                    id = it.id,
                    timestamp = it.timestamp,
                    symbol = it.symbol,
                    side = try { OrderSide.valueOf(it.side) } catch (_: Exception) { OrderSide.BUY },
                    quantity = it.quantity,
                    price = it.price,
                    strategy = it.strategy,
                    status = try { OrderStatus.valueOf(it.status) } catch (_: Exception) { OrderStatus.FILLED },
                    realizedPnl = it.realizedPnl,
                    executionNote = it.note
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun executePaperOrder(
        symbol: String,
        side: OrderSide,
        requestedQuantity: Double,
        price: Double,
        strategyName: String,
        riskConfig: RiskConfig
    ): Result<PaperOrder> = withContext(Dispatchers.IO) {
        val accountEntity = dao.getPaperAccountSync() ?: PaperAccountEntity()
        val account = PaperAccount(
            startingCash = accountEntity.startingCash,
            cash = accountEntity.cash,
            realizedPnl = accountEntity.realizedPnl
        )
        val currentPositionEntities = dao.getAllPositionsSync()
        val positions = currentPositionEntities.map {
            PaperPosition(it.symbol, it.quantity, it.averagePrice, it.currentPrice)
        }

        // Validate Risk Controls
        val riskResult = RiskEngine.validateOrder(
            symbol = symbol,
            isBuy = side == OrderSide.BUY,
            requestedQuantity = requestedQuantity,
            price = price,
            account = account,
            existingPositions = positions,
            config = riskConfig
        )

        if (!riskResult.isAllowed) {
            val rejectedOrder = PaperOrderEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                symbol = symbol,
                side = side.name,
                quantity = requestedQuantity,
                price = price,
                strategy = strategyName,
                status = OrderStatus.REJECTED.name,
                realizedPnl = 0.0,
                note = riskResult.warningMessage ?: "Risk evaluation failed."
            )
            dao.insertPaperOrder(rejectedOrder)
            return@withContext Result.failure(Exception(riskResult.warningMessage ?: "Order rejected by risk management."))
        }

        val execQty = riskResult.adjustedQuantity
        val orderCost = execQty * price
        var realizedPnl = 0.0

        if (side == OrderSide.BUY) {
            val newCash = account.cash - orderCost
            val existingPos = dao.getPositionBySymbol(symbol)

            if (existingPos == null) {
                dao.insertOrUpdatePosition(
                    PaperPositionEntity(
                        symbol = symbol,
                        quantity = execQty,
                        averagePrice = price,
                        currentPrice = price
                    )
                )
            } else {
                val totalShares = existingPos.quantity + execQty
                val totalCost = (existingPos.quantity * existingPos.averagePrice) + orderCost
                val newAvg = if (totalShares > 0) totalCost / totalShares else price
                dao.insertOrUpdatePosition(
                    PaperPositionEntity(
                        symbol = symbol,
                        quantity = totalShares,
                        averagePrice = newAvg,
                        currentPrice = price
                    )
                )
            }

            dao.updatePaperAccount(accountEntity.copy(cash = newCash))
        } else {
            // SELL
            val existingPos = dao.getPositionBySymbol(symbol)
            if (existingPos != null) {
                realizedPnl = (price - existingPos.averagePrice) * execQty
                val remainingQty = existingPos.quantity - execQty
                val newCash = account.cash + orderCost
                val newRealized = account.realizedPnl + realizedPnl

                if (remainingQty <= 0.0001) {
                    dao.deletePosition(symbol)
                } else {
                    dao.insertOrUpdatePosition(
                        existingPos.copy(quantity = remainingQty, currentPrice = price)
                    )
                }
                dao.updatePaperAccount(accountEntity.copy(cash = newCash, realizedPnl = newRealized))
            }
        }

        val noteText = buildString {
            if (riskResult.warningMessage != null) append(riskResult.warningMessage).append(" ")
            append("Simulated execution at $").append(String.format("%.2f", price))
        }

        val filledOrder = PaperOrderEntity(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            symbol = symbol,
            side = side.name,
            quantity = execQty,
            price = price,
            strategy = strategyName,
            status = OrderStatus.FILLED.name,
            realizedPnl = realizedPnl,
            note = noteText
        )
        dao.insertPaperOrder(filledOrder)

        Result.success(
            PaperOrder(
                id = filledOrder.id,
                timestamp = filledOrder.timestamp,
                symbol = filledOrder.symbol,
                side = side,
                quantity = execQty,
                price = price,
                strategy = strategyName,
                status = OrderStatus.FILLED,
                realizedPnl = realizedPnl,
                executionNote = noteText
            )
        )
    }

    suspend fun saveBacktestResult(result: BacktestResult) = withContext(Dispatchers.IO) {
        val record = BacktestRecordEntity(
            id = result.id,
            datasetName = result.datasetName,
            symbol = result.symbol,
            strategyName = result.strategyName,
            initialCapital = result.metrics.initialCapital,
            finalCapital = result.metrics.finalCapital,
            totalReturnPct = result.metrics.totalReturnPct,
            buyAndHoldReturnPct = result.metrics.buyAndHoldReturnPct,
            winRatePct = result.metrics.winRatePct,
            totalTrades = result.metrics.totalTrades,
            maxDrawdownPct = result.metrics.maxDrawdownPct,
            sharpeRatio = result.metrics.sharpeRatio,
            tradesSummaryJson = "${result.trades.size} trades recorded",
            timestamp = result.executionTimestamp
        )
        dao.insertBacktestRecord(record)
    }

    fun getBacktestRecordsFlow(): Flow<List<BacktestRecordEntity>> {
        return dao.getAllBacktestRecords().flowOn(Dispatchers.IO)
    }

    suspend fun resetAllDemoData() = withContext(Dispatchers.IO) {
        dao.resetAllData()
        val existing = dao.getAllDatasets().firstOrNull() ?: emptyList()
        for (ds in existing) {
            dao.deleteBarsForDataset(ds.id)
            dao.deleteDataset(ds.id)
        }
        val demos = SampleDataGenerator.getInitialDemoDatasets()
        for (demo in demos) {
            dao.insertDataset(
                DatasetEntity(
                    id = demo.id,
                    name = demo.name,
                    symbol = demo.symbol,
                    type = demo.type.name,
                    description = demo.description,
                    createdAt = demo.createdAt
                )
            )
            dao.insertBars(
                demo.bars.map { bar ->
                    PriceBarEntity(
                        datasetId = demo.id,
                        date = bar.date,
                        open = bar.open,
                        high = bar.high,
                        low = bar.low,
                        close = bar.close,
                        volume = bar.volume
                    )
                }
            )
        }
    }
}
