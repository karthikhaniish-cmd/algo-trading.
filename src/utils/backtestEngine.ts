import { BacktestConfig, BacktestResult, EquityPoint, PerformanceMetrics, PriceBar, StrategyConfig, TradeRecord } from '../types/trading';
import { generateStrategySignals } from './indicators';

export function runBacktest(
  datasetName: string,
  symbol: string,
  bars: PriceBar[],
  strategyConfig: StrategyConfig,
  config: BacktestConfig
): BacktestResult {
  const { signals } = generateStrategySignals(bars, strategyConfig);

  let cash = config.initialCapital;
  let shares = 0;
  let entryPrice = 0;
  let entryDate = '';
  let entryBarIdx = 0;

  const feeMultiplier = config.feePercent / 100;
  const slippageMultiplier = config.slippagePercent / 100;

  const trades: TradeRecord[] = [];
  const equityCurve: EquityPoint[] = [];

  const initialBenchmarkPrice = bars[0]?.open || 1;
  let peakEquity = config.initialCapital;
  let maxDrawdown = 0;
  const dailyReturns: number[] = [];
  let previousTotalEquity = config.initialCapital;

  for (let i = 0; i < bars.length; i++) {
    const currentBar = bars[i];

    // Check previous bar signal (Zero lookahead bias)
    if (i > 0) {
      const prevSignal = signals[i - 1];

      // Stop-loss and take-profit evaluations
      if (shares > 0) {
        const stopLossHit = config.stopLossPercent !== undefined &&
          currentBar.low <= entryPrice * (1 - config.stopLossPercent / 100);
        const takeProfitHit = config.takeProfitPercent !== undefined &&
          currentBar.high >= entryPrice * (1 + config.takeProfitPercent / 100);

        if (stopLossHit) {
          const exitPrice = entryPrice * (1 - config.stopLossPercent! / 100);
          const gross = shares * exitPrice;
          const fee = gross * feeMultiplier;
          const net = gross - fee;
          const pnlDollar = net - shares * entryPrice;
          const pnlPercent = ((exitPrice - entryPrice) / entryPrice) * 100;
          cash += net;

          trades.push({
            id: `trade_${trades.length + 1}`,
            entryDate,
            entryPrice: Number(entryPrice.toFixed(2)),
            exitDate: currentBar.date,
            exitPrice: Number(exitPrice.toFixed(2)),
            quantity: Number(shares.toFixed(2)),
            pnlDollar: Number(pnlDollar.toFixed(2)),
            pnlPercent: Number(pnlPercent.toFixed(2)),
            exitReason: `Stop Loss (-${config.stopLossPercent}%)`,
            durationBars: i - entryBarIdx
          });
          shares = 0;
        } else if (takeProfitHit) {
          const exitPrice = entryPrice * (1 + config.takeProfitPercent! / 100);
          const gross = shares * exitPrice;
          const fee = gross * feeMultiplier;
          const net = gross - fee;
          const pnlDollar = net - shares * entryPrice;
          const pnlPercent = ((exitPrice - entryPrice) / entryPrice) * 100;
          cash += net;

          trades.push({
            id: `trade_${trades.length + 1}`,
            entryDate,
            entryPrice: Number(entryPrice.toFixed(2)),
            exitDate: currentBar.date,
            exitPrice: Number(exitPrice.toFixed(2)),
            quantity: Number(shares.toFixed(2)),
            pnlDollar: Number(pnlDollar.toFixed(2)),
            pnlPercent: Number(pnlPercent.toFixed(2)),
            exitReason: `Take Profit (+${config.takeProfitPercent}%)`,
            durationBars: i - entryBarIdx
          });
          shares = 0;
        }
      }

      // Execute on Next Bar Open Price + Slippage
      if (prevSignal.signal === 'BUY' && shares === 0) {
        const execPrice = currentBar.open * (1 + slippageMultiplier);
        const capitalToDeploy = cash * (config.positionSizePercent / 100);

        if (capitalToDeploy > 10 && execPrice > 0) {
          const fee = capitalToDeploy * feeMultiplier;
          const netDeploy = capitalToDeploy - fee;
          const sharesBought = netDeploy / execPrice;

          if (sharesBought > 0.001) {
            cash -= capitalToDeploy;
            shares = sharesBought;
            entryPrice = execPrice;
            entryDate = currentBar.date;
            entryBarIdx = i;
          }
        }
      } else if (prevSignal.signal === 'SELL' && shares > 0) {
        const execPrice = currentBar.open * (1 - slippageMultiplier);
        const gross = shares * execPrice;
        const fee = gross * feeMultiplier;
        const net = gross - fee;
        const pnlDollar = net - shares * entryPrice;
        const pnlPercent = ((execPrice - entryPrice) / entryPrice) * 100;

        cash += net;
        trades.push({
          id: `trade_${trades.length + 1}`,
          entryDate,
          entryPrice: Number(entryPrice.toFixed(2)),
          exitDate: currentBar.date,
          exitPrice: Number(execPrice.toFixed(2)),
          quantity: Number(shares.toFixed(2)),
          pnlDollar: Number(pnlDollar.toFixed(2)),
          pnlPercent: Number(pnlPercent.toFixed(2)),
          exitReason: 'Strategy Signal Exit',
          durationBars: i - entryBarIdx
        });
        shares = 0;
      }
    }

    const currentPortfolioVal = cash + shares * currentBar.close;
    peakEquity = Math.max(peakEquity, currentPortfolioVal);
    const drawdown = peakEquity > 0 ? ((peakEquity - currentPortfolioVal) / peakEquity) * 100 : 0;
    maxDrawdown = Math.max(maxDrawdown, drawdown);

    const benchmarkEquity = (config.initialCapital * currentBar.close) / initialBenchmarkPrice;

    equityCurve.push({
      date: currentBar.date,
      equity: Number(currentPortfolioVal.toFixed(2)),
      cash: Number(cash.toFixed(2)),
      drawdownPct: Number(drawdown.toFixed(2)),
      benchmarkEquity: Number(benchmarkEquity.toFixed(2))
    });

    if (i > 0 && previousTotalEquity > 0) {
      dailyReturns.push((currentPortfolioVal - previousTotalEquity) / previousTotalEquity);
    }
    previousTotalEquity = currentPortfolioVal;
  }

  // Liquidate on final bar
  if (shares > 0) {
    const lastBar = bars[bars.length - 1];
    const execPrice = lastBar.close * (1 - slippageMultiplier);
    const gross = shares * execPrice;
    const fee = gross * feeMultiplier;
    const net = gross - fee;
    const pnlDollar = net - shares * entryPrice;
    const pnlPercent = ((execPrice - entryPrice) / entryPrice) * 100;
    cash += net;

    trades.push({
      id: `trade_${trades.length + 1}`,
      entryDate,
      entryPrice: Number(entryPrice.toFixed(2)),
      exitDate: lastBar.date,
      exitPrice: Number(execPrice.toFixed(2)),
      quantity: Number(shares.toFixed(2)),
      pnlDollar: Number(pnlDollar.toFixed(2)),
      pnlPercent: Number(pnlPercent.toFixed(2)),
      exitReason: 'End of Backtest Window',
      durationBars: bars.length - 1 - entryBarIdx
    });
    shares = 0;
  }

  const finalCapital = cash;
  const totalReturnPct = ((finalCapital - config.initialCapital) / config.initialCapital) * 100;
  const lastPrice = bars[bars.length - 1]?.close || initialBenchmarkPrice;
  const buyAndHoldReturnPct = ((lastPrice - initialBenchmarkPrice) / initialBenchmarkPrice) * 100;

  const totalTrades = trades.length;
  const winningTrades = trades.filter(t => t.pnlDollar > 0).length;
  const losingTrades = trades.filter(t => t.pnlDollar < 0).length;
  const winRatePct = totalTrades > 0 ? (winningTrades / totalTrades) * 100 : 0;

  const grossProfit = trades.filter(t => t.pnlDollar > 0).reduce((sum, t) => sum + t.pnlDollar, 0);
  const grossLoss = Math.abs(trades.filter(t => t.pnlDollar < 0).reduce((sum, t) => sum + t.pnlDollar, 0));
  const profitFactor = grossLoss > 0 ? grossProfit / grossLoss : grossProfit > 0 ? 99.9 : 0;

  // Annualized Sharpe
  const dailyRf = 0.02 / 252;
  const excess = dailyReturns.map(r => r - dailyRf);
  const meanExcess = excess.length > 0 ? excess.reduce((a, b) => a + b, 0) / excess.length : 0;
  const variance = excess.length > 1
    ? excess.reduce((a, b) => a + Math.pow(b - meanExcess, 2), 0) / (excess.length - 1)
    : 0;
  const std = Math.sqrt(variance);
  const sharpeRatio = std > 0 ? (meanExcess / std) * Math.sqrt(252) : 0;

  const metrics: PerformanceMetrics = {
    initialCapital: config.initialCapital,
    finalCapital: Number(finalCapital.toFixed(2)),
    totalReturnPct: Number(totalReturnPct.toFixed(2)),
    buyAndHoldReturnPct: Number(buyAndHoldReturnPct.toFixed(2)),
    totalTrades,
    winningTrades,
    losingTrades,
    winRatePct: Number(winRatePct.toFixed(1)),
    profitFactor: Number(profitFactor.toFixed(2)),
    maxDrawdownPct: Number(maxDrawdown.toFixed(2)),
    sharpeRatio: Number(sharpeRatio.toFixed(2))
  };

  return {
    id: `bt_${Date.now()}`,
    datasetName,
    symbol,
    strategyName: strategyConfig.type.replace('_', ' '),
    config,
    strategyConfig,
    metrics,
    trades,
    equityCurve,
    timestamp: Date.now()
  };
}
