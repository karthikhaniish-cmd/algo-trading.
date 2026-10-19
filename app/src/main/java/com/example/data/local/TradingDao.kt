package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TradingDao {

    // Datasets
    @Query("SELECT * FROM datasets ORDER BY createdAt DESC")
    fun getAllDatasets(): Flow<List<DatasetEntity>>

    @Query("SELECT * FROM datasets WHERE id = :datasetId LIMIT 1")
    suspend fun getDatasetById(datasetId: String): DatasetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: DatasetEntity)

    @Query("DELETE FROM datasets WHERE id = :datasetId")
    suspend fun deleteDataset(datasetId: String)

    // Price Bars
    @Query("SELECT * FROM price_bars WHERE datasetId = :datasetId ORDER BY date ASC")
    fun getBarsForDataset(datasetId: String): Flow<List<PriceBarEntity>>

    @Query("SELECT * FROM price_bars WHERE datasetId = :datasetId ORDER BY date ASC")
    suspend fun getBarsForDatasetSync(datasetId: String): List<PriceBarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBars(bars: List<PriceBarEntity>)

    @Query("DELETE FROM price_bars WHERE datasetId = :datasetId")
    suspend fun deleteBarsForDataset(datasetId: String)

    // Paper Orders
    @Query("SELECT * FROM paper_orders ORDER BY timestamp DESC")
    fun getAllPaperOrders(): Flow<List<PaperOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaperOrder(order: PaperOrderEntity)

    @Query("DELETE FROM paper_orders")
    suspend fun clearPaperOrders()

    // Paper Positions
    @Query("SELECT * FROM paper_positions WHERE quantity > 0.0")
    fun getAllPositions(): Flow<List<PaperPositionEntity>>

    @Query("SELECT * FROM paper_positions WHERE quantity > 0.0")
    suspend fun getAllPositionsSync(): List<PaperPositionEntity>

    @Query("SELECT * FROM paper_positions WHERE symbol = :symbol LIMIT 1")
    suspend fun getPositionBySymbol(symbol: String): PaperPositionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePosition(position: PaperPositionEntity)

    @Query("DELETE FROM paper_positions WHERE symbol = :symbol")
    suspend fun deletePosition(symbol: String)

    @Query("DELETE FROM paper_positions")
    suspend fun clearPositions()

    // Paper Account
    @Query("SELECT * FROM paper_account WHERE id = 1 LIMIT 1")
    fun getPaperAccount(): Flow<PaperAccountEntity?>

    @Query("SELECT * FROM paper_account WHERE id = 1 LIMIT 1")
    suspend fun getPaperAccountSync(): PaperAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePaperAccount(account: PaperAccountEntity)

    // Backtest Records
    @Query("SELECT * FROM backtest_records ORDER BY timestamp DESC")
    fun getAllBacktestRecords(): Flow<List<BacktestRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBacktestRecord(record: BacktestRecordEntity)

    @Query("DELETE FROM backtest_records")
    suspend fun clearBacktestRecords()

    @Transaction
    suspend fun resetAllData() {
        clearPaperOrders()
        clearPositions()
        clearBacktestRecords()
        updatePaperAccount(PaperAccountEntity(id = 1, startingCash = 10000.0, cash = 10000.0, realizedPnl = 0.0))
    }
}
