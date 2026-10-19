package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TradingDatabase
import com.example.data.repository.TradingRepository
import com.example.domain.backtest.BacktestEngine
import com.example.domain.strategy.StrategyEngine
import com.example.model.BacktestConfig
import com.example.model.BacktestResult
import com.example.model.CsvValidationResult
import com.example.model.IndicatorSeries
import com.example.model.OrderSide
import com.example.model.PaperAccount
import com.example.model.PaperOrder
import com.example.model.PaperPosition
import com.example.model.PriceBar
import com.example.model.RiskConfig
import com.example.model.SignalType
import com.example.model.StockDataset
import com.example.model.StrategyConfig
import com.example.model.StrategyType
import com.example.model.TradingSignal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class TradingUiState(
    val datasets: List<StockDataset> = emptyList(),
    val selectedDataset: StockDataset? = null,
    val bars: List<PriceBar> = emptyList(),
    val indicatorSeries: IndicatorSeries? = null,
    val latestSignal: TradingSignal? = null,
    val strategyConfig: StrategyConfig = StrategyConfig(),
    val backtestConfig: BacktestConfig = BacktestConfig(),
    val latestBacktestResult: BacktestResult? = null,
    val isBacktesting: Boolean = false,
    val paperAccount: PaperAccount = PaperAccount(),
    val paperPositions: List<PaperPosition> = emptyList(),
    val paperOrders: List<PaperOrder> = emptyList(),
    val riskConfig: RiskConfig = RiskConfig(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val csvValidationResult: CsvValidationResult? = null,
    // Step simulation index for Paper Trading
    val paperSimulationBarIndex: Int = 0
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TradingRepository

    private val _uiState = MutableStateFlow(TradingUiState(isLoading = true))
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    init {
        val database = TradingDatabase.getDatabase(application)
        repository = TradingRepository(database.tradingDao())

        viewModelScope.launch {
            repository.ensureInitialized()
            observeData()
        }
    }

    private fun observeData() {
        // Observe Datasets
        viewModelScope.launch {
            repository.getDatasets().collectLatest { datasets ->
                _uiState.value = _uiState.value.copy(datasets = datasets)
                if (_uiState.value.selectedDataset == null && datasets.isNotEmpty()) {
                    selectDataset(datasets.first().id)
                }
            }
        }

        // Observe Paper Account
        viewModelScope.launch {
            repository.getPaperAccountFlow().collectLatest { account ->
                _uiState.value = _uiState.value.copy(paperAccount = account)
            }
        }

        // Observe Positions
        viewModelScope.launch {
            repository.getPositionsFlow().collectLatest { positions ->
                // Update current price in positions from active dataset if matching
                val activePrice = _uiState.value.bars.lastOrNull()?.close
                val activeSymbol = _uiState.value.selectedDataset?.symbol
                val updatedPositions = positions.map { pos ->
                    if (pos.symbol.equals(activeSymbol, ignoreCase = true) && activePrice != null) {
                        pos.copy(currentPrice = activePrice)
                    } else pos
                }
                _uiState.value = _uiState.value.copy(paperPositions = updatedPositions)
            }
        }

        // Observe Paper Orders
        viewModelScope.launch {
            repository.getOrdersFlow().collectLatest { orders ->
                _uiState.value = _uiState.value.copy(paperOrders = orders)
            }
        }
    }

    fun selectDataset(datasetId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val fullDataset = repository.getFullDataset(datasetId)
            if (fullDataset != null) {
                val series = StrategyEngine.generateSeriesAndSignals(
                    fullDataset.bars,
                    _uiState.value.strategyConfig
                )
                val latestSig = series.signals.lastOrNull()
                _uiState.value = _uiState.value.copy(
                    selectedDataset = fullDataset,
                    bars = fullDataset.bars,
                    indicatorSeries = series,
                    latestSignal = latestSig,
                    paperSimulationBarIndex = maxOf(0, fullDataset.bars.size - 1),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateStrategyConfig(newConfig: StrategyConfig) {
        val bars = _uiState.value.bars
        val series = StrategyEngine.generateSeriesAndSignals(bars, newConfig)
        val latestSig = series.signals.lastOrNull()
        _uiState.value = _uiState.value.copy(
            strategyConfig = newConfig,
            indicatorSeries = series,
            latestSignal = latestSig
        )
    }

    fun updateBacktestConfig(newConfig: BacktestConfig) {
        _uiState.value = _uiState.value.copy(backtestConfig = newConfig)
    }

    fun updateRiskConfig(newConfig: RiskConfig) {
        _uiState.value = _uiState.value.copy(riskConfig = newConfig)
    }

    fun runBacktest() {
        val bars = _uiState.value.bars
        val dataset = _uiState.value.selectedDataset ?: return
        if (bars.isEmpty()) {
            _uiState.value = _uiState.value.copy(userMessage = "No market data available for backtesting.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBacktesting = true)
            val result = BacktestEngine.runBacktest(
                datasetName = dataset.name,
                symbol = dataset.symbol,
                bars = bars,
                strategyConfig = _uiState.value.strategyConfig,
                config = _uiState.value.backtestConfig
            )
            repository.saveBacktestResult(result)
            _uiState.value = _uiState.value.copy(
                latestBacktestResult = result,
                isBacktesting = false,
                userMessage = "Backtest completed: Return ${String.format("%.2f", result.metrics.totalReturnPct)}%, ${result.metrics.totalTrades} trades."
            )
        }
    }

    fun executeManualPaperOrder(symbol: String, side: OrderSide, quantity: Double, price: Double) {
        viewModelScope.launch {
            val result = repository.executePaperOrder(
                symbol = symbol,
                side = side,
                requestedQuantity = quantity,
                price = price,
                strategyName = "Manual Order",
                riskConfig = _uiState.value.riskConfig
            )
            result.onSuccess { order ->
                _uiState.value = _uiState.value.copy(
                    userMessage = "Paper Order Filled: ${order.side} ${order.quantity} $symbol @ $${String.format("%.2f", order.price)}"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    userMessage = "Order Failed: ${err.message}"
                )
            }
        }
    }

    fun stepSimulationPaperTrade() {
        val bars = _uiState.value.bars
        if (bars.isEmpty()) return

        val series = _uiState.value.indicatorSeries ?: return
        val currentIndex = _uiState.value.paperSimulationBarIndex
        val currentBar = bars.getOrNull(currentIndex) ?: return
        val currentSignal = series.signals.getOrNull(currentIndex) ?: return
        val symbol = _uiState.value.selectedDataset?.symbol ?: "STOCK"

        viewModelScope.launch {
            if (currentSignal.signal == SignalType.BUY) {
                // Execute Buy with 20% of cash
                val price = currentBar.close
                val cash = _uiState.value.paperAccount.cash
                val deploy = cash * 0.20
                val shares = if (price > 0) deploy / price else 0.0
                if (shares > 0.001) {
                    repository.executePaperOrder(
                        symbol = symbol,
                        side = OrderSide.BUY,
                        requestedQuantity = shares,
                        price = price,
                        strategyName = _uiState.value.strategyConfig.strategyType.displayName,
                        riskConfig = _uiState.value.riskConfig
                    )
                }
            } else if (currentSignal.signal == SignalType.SELL) {
                // Sell current position if any
                val pos = _uiState.value.paperPositions.find { it.symbol.equals(symbol, ignoreCase = true) }
                if (pos != null && pos.quantity > 0) {
                    repository.executePaperOrder(
                        symbol = symbol,
                        side = OrderSide.SELL,
                        requestedQuantity = pos.quantity,
                        price = currentBar.close,
                        strategyName = _uiState.value.strategyConfig.strategyType.displayName,
                        riskConfig = _uiState.value.riskConfig
                    )
                }
            }
            _uiState.value = _uiState.value.copy(
                userMessage = "Step Bar ${currentBar.date}: Signal is ${currentSignal.signal} ($${currentBar.close})"
            )
        }
    }

    fun importCsv(symbol: String, name: String, description: String, csvContent: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.importCsvDataset(symbol, name, description, csvContent)
            _uiState.value = _uiState.value.copy(
                csvValidationResult = result,
                isLoading = false,
                userMessage = if (result.isValid) "Dataset '$symbol' imported successfully (${result.parsedCount} bars)."
                else "CSV validation failed: ${result.errors.firstOrNull()}"
            )
        }
    }

    fun createSynthetic(
        symbol: String,
        name: String,
        description: String,
        startPrice: Double,
        volatility: Double,
        drift: Double,
        barsCount: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val dataset = repository.createSyntheticDataset(
                symbol, name, description, startPrice, volatility, drift, barsCount
            )
            selectDataset(dataset.id)
            _uiState.value = _uiState.value.copy(
                userMessage = "Generated synthetic dataset '$symbol' with $barsCount bars."
            )
        }
    }

    fun deleteDataset(datasetId: String) {
        viewModelScope.launch {
            repository.deleteDataset(datasetId)
            _uiState.value = _uiState.value.copy(userMessage = "Dataset deleted.")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.resetAllDemoData()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                latestBacktestResult = null,
                userMessage = "All paper trading and datasets have been reset to factory demo state."
            )
        }
    }

    fun clearUserMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }
}
