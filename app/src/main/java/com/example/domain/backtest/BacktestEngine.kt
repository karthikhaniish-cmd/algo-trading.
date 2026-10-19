package com.example.domain.backtest

import com.example.domain.strategy.StrategyEngine
import com.example.model.BacktestConfig
import com.example.model.BacktestResult
import com.example.model.EquityPoint
import com.example.model.PerformanceMetrics
import com.example.model.PriceBar
import com.example.model.SignalType
import com.example.model.StrategyConfig
import com.example.model.TradeRecord
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object BacktestEngine {

    fun runBacktest(
        datasetName: String,
        symbol: String,
        bars: List<PriceBar>,
        strategyConfig: StrategyConfig,
        config: BacktestConfig
    ): BacktestResult {
        if (bars.size < 2) {
            val emptyMetrics = PerformanceMetrics(
                initialCapital = config.initialCapital,
                finalCapital = config.initialCapital,
                totalReturnPct = 0.0,
                buyAndHoldReturnPct = 0.0,
                totalTrades = 0,
                winningTrades = 0,
                losingTrades = 0,
                winRatePct = 0.0,
                profitFactor = 0.0,
                maxDrawdownPct = 0.0,
                sharpeRatio = 0.0,
                averageTradePnlPct = 0.0,
                maxConsecutiveWins = 0,
                maxConsecutiveLosses = 0
            )
            return BacktestResult(
                id = UUID.randomUUID().toString(),
                datasetName = datasetName,
                symbol = symbol,
                strategyName = strategyConfig.strategyType.displayName,
                config = config,
                strategyConfig = strategyConfig,
                metrics = emptyMetrics,
                trades = emptyList(),
                equityCurve = emptyList()
            )
        }

        val indicatorSeries = StrategyEngine.generateSeriesAndSignals(bars, strategyConfig)
        val signals = indicatorSeries.signals

        var cash = config.initialCapital
        var shares = 0.0
        var entryPrice = 0.0
        var entryBarIndex = 0
        var entryDate = ""

        val trades = ArrayList<TradeRecord>()
        val equityCurve = ArrayList<EquityPoint>(bars.size)

        val benchmarkInitialPrice = bars.first().open
        var peakEquity = config.initialCapital
        var maxDrawdownPct = 0.0
        val dailyReturns = ArrayList<Double>()
        var previousTotalEquity = config.initialCapital

        val feeMultiplier = config.feePercent / 100.0
        val slippageMultiplier = config.slippagePercent / 100.0

        for (i in bars.indices) {
            val currentBar = bars[i]

            // Step 1: Check pending signal from PREVIOUS bar (i - 1) to eliminate look-ahead bias!
            if (i > 0) {
                val previousSignal = signals[i - 1]

                // Check stop loss and take profit for existing open position
                if (shares > 0.0) {
                    val stopLossHit = config.stopLossPct != null &&
                            currentBar.low <= entryPrice * (1.0 - (config.stopLossPct / 100.0))
                    val takeProfitHit = config.takeProfitPct != null &&
                            currentBar.high >= entryPrice * (1.0 + (config.takeProfitPct / 100.0))

                    if (stopLossHit) {
                        val exitPrice = entryPrice * (1.0 - (config.stopLossPct!! / 100.0))
                        val exitProceeds = shares * exitPrice * (1.0 - feeMultiplier)
                        val pnlDollar = exitProceeds - (shares * entryPrice)
                        val pnlPercent = ((exitPrice - entryPrice) / entryPrice) * 100.0
                        cash += exitProceeds

                        trades.add(
                            TradeRecord(
                                id = UUID.randomUUID().toString(),
                                entryDate = entryDate,
                                entryPrice = entryPrice,
                                exitDate = currentBar.date,
                                exitPrice = exitPrice,
                                quantity = shares,
                                isLong = true,
                                pnlDollar = pnlDollar,
                                pnlPercent = pnlPercent,
                                exitReason = "Stop Loss (-${config.stopLossPct}%)",
                                durationBars = i - entryBarIndex
                            )
                        )
                        shares = 0.0
                    } else if (takeProfitHit) {
                        val exitPrice = entryPrice * (1.0 + (config.takeProfitPct!! / 100.0))
                        val exitProceeds = shares * exitPrice * (1.0 - feeMultiplier)
                        val pnlDollar = exitProceeds - (shares * entryPrice)
                        val pnlPercent = ((exitPrice - entryPrice) / entryPrice) * 100.0
                        cash += exitProceeds

                        trades.add(
                            TradeRecord(
                                id = UUID.randomUUID().toString(),
                                entryDate = entryDate,
                                entryPrice = entryPrice,
                                exitDate = currentBar.date,
                                exitPrice = exitPrice,
                                quantity = shares,
                                isLong = true,
                                pnlDollar = pnlDollar,
                                pnlPercent = pnlPercent,
                                exitReason = "Take Profit (+${config.takeProfitPct}%)",
                                durationBars = i - entryBarIndex
                            )
                        )
                        shares = 0.0
                    }
                }

                // If still holding or neutral, execute next-bar signal from previous bar's close
                if (previousSignal.signal == SignalType.BUY && shares == 0.0) {
                    // Execute Buy at next bar's Open price + slippage
                    val executionPrice = currentBar.open * (1.0 + slippageMultiplier)
                    val capitalToDeploy = cash * (config.positionSizePct / 100.0)

                    if (capitalToDeploy > 10.0 && executionPrice > 0.0) {
                        val fee = capitalToDeploy * feeMultiplier
                        val netDeploy = capitalToDeploy - fee
                        val sharesBought = netDeploy / executionPrice

                        if (sharesBought > 0.0001) {
                            cash -= capitalToDeploy
                            shares = sharesBought
                            entryPrice = executionPrice
                            entryBarIndex = i
                            entryDate = currentBar.date
                        }
                    }
                } else if (previousSignal.signal == SignalType.SELL && shares > 0.0) {
                    // Execute Sell at next bar's Open price - slippage
                    val executionPrice = currentBar.open * (1.0 - slippageMultiplier)
                    val grossProceeds = shares * executionPrice
                    val fee = grossProceeds * feeMultiplier
                    val netProceeds = grossProceeds - fee
                    val pnlDollar = netProceeds - (shares * entryPrice)
                    val pnlPercent = ((executionPrice - entryPrice) / entryPrice) * 100.0

                    cash += netProceeds
                    trades.add(
                        TradeRecord(
                            id = UUID.randomUUID().toString(),
                            entryDate = entryDate,
                            entryPrice = entryPrice,
                            exitDate = currentBar.date,
                            exitPrice = executionPrice,
                            quantity = shares,
                            isLong = true,
                            pnlDollar = pnlDollar,
                            pnlPercent = pnlPercent,
                            exitReason = "Strategy Signal",
                            durationBars = i - entryBarIndex
                        )
                    )
                    shares = 0.0
                }
            }

            // Mark-to-market equity at end of bar
            val currentPortfolioValue = cash + (shares * currentBar.close)
            if (currentPortfolioValue > peakEquity) {
                peakEquity = currentPortfolioValue
            }
            val currentDrawdown = if (peakEquity > 0.0) {
                ((peakEquity - currentPortfolioValue) / peakEquity) * 100.0
            } else 0.0

            if (currentDrawdown > maxDrawdownPct) {
                maxDrawdownPct = currentDrawdown
            }

            // Benchmark comparison (Buy & hold 100% of initial capital in the stock)
            val benchmarkEquity = if (benchmarkInitialPrice > 0.0) {
                config.initialCapital * (currentBar.close / benchmarkInitialPrice)
            } else config.initialCapital

            equityCurve.add(
                EquityPoint(
                    date = currentBar.date,
                    equity = currentPortfolioValue,
                    cash = cash,
                    drawdownPct = currentDrawdown,
                    benchmarkEquity = benchmarkEquity
                )
            )

            // Calculate daily return for Sharpe ratio
            if (i > 0 && previousTotalEquity > 0.0) {
                val dailyReturn = (currentPortfolioValue - previousTotalEquity) / previousTotalEquity
                dailyReturns.add(dailyReturn)
            }
            previousTotalEquity = currentPortfolioValue
        }

        // Close out any remaining position on the final bar at close price
        if (shares > 0.0) {
            val lastBar = bars.last()
            val exitPrice = lastBar.close * (1.0 - slippageMultiplier)
            val gross = shares * exitPrice
            val fee = gross * feeMultiplier
            val net = gross - fee
            val pnlDollar = net - (shares * entryPrice)
            val pnlPercent = ((exitPrice - entryPrice) / entryPrice) * 100.0
            cash += net

            trades.add(
                TradeRecord(
                    id = UUID.randomUUID().toString(),
                    entryDate = entryDate,
                    entryPrice = entryPrice,
                    exitDate = lastBar.date,
                    exitPrice = exitPrice,
                    quantity = shares,
                    isLong = true,
                    pnlDollar = pnlDollar,
                    pnlPercent = pnlPercent,
                    exitReason = "End of Backtest Window",
                    durationBars = bars.size - 1 - entryBarIndex
                )
            )
            shares = 0.0
        }

        val finalCapital = cash
        val totalReturnPct = ((finalCapital - config.initialCapital) / config.initialCapital) * 100.0

        val buyAndHoldFinal = if (benchmarkInitialPrice > 0.0) {
            config.initialCapital * (bars.last().close / benchmarkInitialPrice)
        } else config.initialCapital
        val buyAndHoldReturnPct = ((buyAndHoldFinal - config.initialCapital) / config.initialCapital) * 100.0

        val totalTrades = trades.size
        val winningTrades = trades.count { it.pnlDollar > 0.0 }
        val losingTrades = trades.count { it.pnlDollar < 0.0 }
        val winRatePct = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100.0 else 0.0

        val grossProfit = trades.filter { it.pnlDollar > 0.0 }.sumOf { it.pnlDollar }
        val grossLoss = trades.filter { it.pnlDollar < 0.0 }.sumOf { -it.pnlDollar }
        val profitFactor = if (grossLoss > 0.0) grossProfit / grossLoss else if (grossProfit > 0.0) 99.9 else 0.0

        val avgPnlPct = if (totalTrades > 0) trades.sumOf { it.pnlPercent } / totalTrades else 0.0

        // Annualized Sharpe Ratio calculation (assuming risk-free rate 2% per year = 0.02 / 252 daily)
        val dailyRf = 0.02 / 252.0
        val excessReturns = dailyReturns.map { it - dailyRf }
        val meanExcess = if (excessReturns.isNotEmpty()) excessReturns.average() else 0.0
        val variance = if (excessReturns.size > 1) {
            excessReturns.map { (it - meanExcess) * (it - meanExcess) }.sum() / (excessReturns.size - 1)
        } else 0.0
        val stdDev = sqrt(variance)
        val sharpeRatio = if (stdDev > 0.000001) (meanExcess / stdDev) * sqrt(252.0) else 0.0

        // Consecutive wins / losses
        var maxConsecWins = 0
        var maxConsecLosses = 0
        var currentWins = 0
        var currentLosses = 0
        for (trade in trades) {
            if (trade.pnlDollar > 0) {
                currentWins++
                currentLosses = 0
                maxConsecWins = max(maxConsecWins, currentWins)
            } else if (trade.pnlDollar < 0) {
                currentLosses++
                currentWins = 0
                maxConsecLosses = max(maxConsecLosses, currentLosses)
            }
        }

        val performanceMetrics = PerformanceMetrics(
            initialCapital = config.initialCapital,
            finalCapital = finalCapital,
            totalReturnPct = totalReturnPct,
            buyAndHoldReturnPct = buyAndHoldReturnPct,
            totalTrades = totalTrades,
            winningTrades = winningTrades,
            losingTrades = losingTrades,
            winRatePct = winRatePct,
            profitFactor = profitFactor,
            maxDrawdownPct = maxDrawdownPct,
            sharpeRatio = sharpeRatio,
            averageTradePnlPct = avgPnlPct,
            maxConsecutiveWins = maxConsecWins,
            maxConsecutiveLosses = maxConsecLosses
        )

        return BacktestResult(
            id = UUID.randomUUID().toString(),
            datasetName = datasetName,
            symbol = symbol,
            strategyName = strategyConfig.strategyType.displayName,
            config = config,
            strategyConfig = strategyConfig,
            metrics = performanceMetrics,
            trades = trades,
            equityCurve = equityCurve
        )
    }
}
