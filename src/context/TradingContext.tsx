import React, { createContext, useContext, useState, useMemo, useEffect } from 'react';
import {
  BacktestConfig,
  BacktestResult,
  PaperAccount,
  PaperOrder,
  PaperPosition,
  PriceBar,
  RiskConfig,
  StockDataset,
  StrategyConfig,
  TradingSignal
} from '../types/trading';
import { getInitialDemoDatasets, generateSyntheticDataset } from '../utils/syntheticData';
import { generateStrategySignals } from '../utils/indicators';
import { runBacktest as executeBacktest } from '../utils/backtestEngine';
import { parseAndValidateCsv, CsvValidationResult } from '../utils/csvParser';

interface TradingContextType {
  datasets: StockDataset[];
  selectedDataset: StockDataset;
  bars: PriceBar[];
  strategyConfig: StrategyConfig;
  signals: TradingSignal[];
  fastMa: (number | null)[];
  slowMa: (number | null)[];
  rsi: (number | null)[];
  latestSignal: TradingSignal | null;
  backtestConfig: BacktestConfig;
  latestBacktest: BacktestResult | null;
  isBacktesting: boolean;
  paperAccount: PaperAccount;
  paperPositions: PaperPosition[];
  paperOrders: PaperOrder[];
  riskConfig: RiskConfig;
  activeTab: string;
  setActiveTab: (tab: string) => void;
  selectDataset: (id: string) => void;
  updateStrategy: (config: Partial<StrategyConfig>) => void;
  updateBacktestConfig: (config: Partial<BacktestConfig>) => void;
  updateRiskConfig: (config: Partial<RiskConfig>) => void;
  runBacktest: () => void;
  placePaperOrder: (symbol: string, side: 'BUY' | 'SELL', quantity: number, price: number) => { success: boolean; message: string };
  stepSimulationTrade: () => void;
  importCsvDataset: (symbol: string, name: string, desc: string, csv: string) => CsvValidationResult;
  createSyntheticDataset: (symbol: string, name: string, desc: string, startPrice: number, vol: number, drift: number, bars: number) => void;
  deleteDataset: (id: string) => void;
  resetAllDemoData: () => void;
}

const TradingContext = createContext<TradingContextType | undefined>(undefined);

export const TradingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const initialDatasets = useMemo(() => getInitialDemoDatasets(), []);
  const [datasets, setDatasets] = useState<StockDataset[]>(initialDatasets);
  const [selectedDatasetId, setSelectedDatasetId] = useState<string>(initialDatasets[0].id);
  const [activeTab, setActiveTab] = useState<string>('dashboard');

  const selectedDataset = useMemo(() => {
    return datasets.find(d => d.id === selectedDatasetId) || datasets[0];
  }, [datasets, selectedDatasetId]);

  const bars = selectedDataset.bars;

  // Strategy State
  const [strategyConfig, setStrategyConfig] = useState<StrategyConfig>({
    type: 'SMA_CROSSOVER',
    fastPeriod: 9,
    slowPeriod: 21,
    rsiPeriod: 14,
    rsiOversold: 30,
    rsiOverbought: 70
  });

  // Calculate Indicator Signals
  const { fastMa, slowMa, rsi, signals } = useMemo(() => {
    return generateStrategySignals(bars, strategyConfig);
  }, [bars, strategyConfig]);

  const latestSignal = signals[signals.length - 1] || null;

  // Backtest State
  const [backtestConfig, setBacktestConfig] = useState<BacktestConfig>({
    initialCapital: 10000,
    feePercent: 0.1,
    slippagePercent: 0.05,
    positionSizePercent: 100,
    stopLossPercent: 3.0,
    takeProfitPercent: 6.0
  });

  const [latestBacktest, setLatestBacktest] = useState<BacktestResult | null>(null);
  const [isBacktesting, setIsBacktesting] = useState(false);

  // Paper Trading State
  const [paperAccount, setPaperAccount] = useState<PaperAccount>({
    startingCash: 10000,
    cash: 10000,
    realizedPnl: 0
  });
  const [paperPositions, setPaperPositions] = useState<PaperPosition[]>([]);
  const [paperOrders, setPaperOrders] = useState<PaperOrder[]>([]);

  // Risk Management
  const [riskConfig, setRiskConfig] = useState<RiskConfig>({
    maxPositionSizePct: 25,
    maxPortfolioExposurePct: 80,
    defaultStopLossPct: 3.0,
    defaultTakeProfitPct: 6.0
  });

  // Update position current prices when asset price moves
  useEffect(() => {
    const activePrice = bars[bars.length - 1]?.close;
    if (!activePrice) return;

    setPaperPositions(prev =>
      prev.map(p => {
        if (p.symbol.toUpperCase() === selectedDataset.symbol.toUpperCase()) {
          const marketValue = p.quantity * activePrice;
          const cost = p.quantity * p.averagePrice;
          const unrealizedPnl = marketValue - cost;
          const unrealizedPnlPct = cost > 0 ? (unrealizedPnl / cost) * 100 : 0;
          return {
            ...p,
            currentPrice: activePrice,
            marketValue: Number(marketValue.toFixed(2)),
            unrealizedPnl: Number(unrealizedPnl.toFixed(2)),
            unrealizedPnlPct: Number(unrealizedPnlPct.toFixed(2))
          };
        }
        return p;
      })
    );
  }, [bars, selectedDataset.symbol]);

  const selectDataset = (id: string) => {
    setSelectedDatasetId(id);
  };

  const updateStrategy = (config: Partial<StrategyConfig>) => {
    setStrategyConfig(prev => ({ ...prev, ...config }));
  };

  const updateBacktestConfig = (config: Partial<BacktestConfig>) => {
    setBacktestConfig(prev => ({ ...prev, ...config }));
  };

  const updateRiskConfig = (config: Partial<RiskConfig>) => {
    setRiskConfig(prev => ({ ...prev, ...config }));
  };

  const runBacktest = () => {
    setIsBacktesting(true);
    setTimeout(() => {
      const result = executeBacktest(
        selectedDataset.name,
        selectedDataset.symbol,
        bars,
        strategyConfig,
        backtestConfig
      );
      setLatestBacktest(result);
      setIsBacktesting(false);
    }, 200);
  };

  const placePaperOrder = (
    symbol: string,
    side: 'BUY' | 'SELL',
    quantity: number,
    price: number
  ): { success: boolean; message: string } => {
    if (quantity <= 0 || price <= 0) {
      return { success: false, message: 'Invalid quantity or price.' };
    }

    const totalPortfolioVal = paperAccount.cash + paperPositions.reduce((sum, p) => sum + p.marketValue, 0);

    if (side === 'BUY') {
      const cost = quantity * price;

      // 1. Cash Check
      if (cost > paperAccount.cash) {
        return { success: false, message: `Insufficient virtual cash ($${paperAccount.cash.toFixed(2)} available, $${cost.toFixed(2)} required).` };
      }

      // 2. Max Position Size Check
      const existingPos = paperPositions.find(p => p.symbol.toUpperCase() === symbol.toUpperCase());
      const currentAssetVal = existingPos ? existingPos.marketValue : 0;
      const maxAllowed = totalPortfolioVal * (riskConfig.maxPositionSizePct / 100);

      if (currentAssetVal + cost > maxAllowed * 1.05) {
        return {
          success: false,
          message: `Risk Limit Exceeded: Order exceeds max position cap (${riskConfig.maxPositionSizePct}% = $${maxAllowed.toFixed(2)}).`
        };
      }

      // Execute Buy
      setPaperAccount(prev => ({ ...prev, cash: prev.cash - cost }));

      setPaperPositions(prev => {
        const found = prev.find(p => p.symbol.toUpperCase() === symbol.toUpperCase());
        if (found) {
          const newQty = found.quantity + quantity;
          const newCost = found.quantity * found.averagePrice + cost;
          const newAvg = newCost / newQty;
          return prev.map(p =>
            p.symbol.toUpperCase() === symbol.toUpperCase()
              ? {
                  ...p,
                  quantity: newQty,
                  averagePrice: Number(newAvg.toFixed(2)),
                  currentPrice: price,
                  marketValue: Number((newQty * price).toFixed(2))
                }
              : p
          );
        } else {
          return [
            ...prev,
            {
              symbol: symbol.toUpperCase(),
              quantity,
              averagePrice: price,
              currentPrice: price,
              marketValue: Number((quantity * price).toFixed(2)),
              unrealizedPnl: 0,
              unrealizedPnlPct: 0
            }
          ];
        }
      });

      const newOrder: PaperOrder = {
        id: `ord_${Date.now()}`,
        timestamp: Date.now(),
        symbol: symbol.toUpperCase(),
        side: 'BUY',
        quantity,
        price,
        strategy: strategyConfig.type.replace('_', ' '),
        status: 'FILLED',
        realizedPnl: 0,
        note: `Simulated market buy of ${quantity} shares @ $${price.toFixed(2)}`
      };
      setPaperOrders(prev => [newOrder, ...prev]);

      return { success: true, message: `Filled BUY: ${quantity} ${symbol} @ $${price.toFixed(2)}` };
    } else {
      // SELL
      const existingPos = paperPositions.find(p => p.symbol.toUpperCase() === symbol.toUpperCase());
      if (!existingPos || existingPos.quantity < quantity) {
        return { success: false, message: `Insufficient shares held (${existingPos?.quantity || 0} shares). Short selling is disabled.` };
      }

      const proceeds = quantity * price;
      const realizedPnl = (price - existingPos.averagePrice) * quantity;

      setPaperAccount(prev => ({
        ...prev,
        cash: prev.cash + proceeds,
        realizedPnl: prev.realizedPnl + realizedPnl
      }));

      setPaperPositions(prev => {
        return prev
          .map(p => {
            if (p.symbol.toUpperCase() === symbol.toUpperCase()) {
              const remaining = p.quantity - quantity;
              return {
                ...p,
                quantity: remaining,
                marketValue: Number((remaining * price).toFixed(2))
              };
            }
            return p;
          })
          .filter(p => p.quantity > 0.0001);
      });

      const newOrder: PaperOrder = {
        id: `ord_${Date.now()}`,
        timestamp: Date.now(),
        symbol: symbol.toUpperCase(),
        side: 'SELL',
        quantity,
        price,
        strategy: strategyConfig.type.replace('_', ' '),
        status: 'FILLED',
        realizedPnl: Number(realizedPnl.toFixed(2)),
        note: `Simulated market sell of ${quantity} shares @ $${price.toFixed(2)}`
      };
      setPaperOrders(prev => [newOrder, ...prev]);

      return {
        success: true,
        message: `Filled SELL: ${quantity} ${symbol} @ $${price.toFixed(2)} (P&L: ${realizedPnl >= 0 ? '+' : ''}$${realizedPnl.toFixed(2)})`
      };
    }
  };

  const stepSimulationTrade = () => {
    if (!latestSignal || bars.length === 0) return;
    const price = bars[bars.length - 1].close;

    if (latestSignal.signal === 'BUY') {
      const budget = paperAccount.cash * 0.25;
      const qty = Math.floor(budget / price);
      if (qty >= 1) {
        placePaperOrder(selectedDataset.symbol, 'BUY', qty, price);
      }
    } else if (latestSignal.signal === 'SELL') {
      const pos = paperPositions.find(p => p.symbol.toUpperCase() === selectedDataset.symbol.toUpperCase());
      if (pos && pos.quantity > 0) {
        placePaperOrder(selectedDataset.symbol, 'SELL', pos.quantity, price);
      }
    }
  };

  const importCsvDataset = (symbol: string, name: string, desc: string, csv: string): CsvValidationResult => {
    const res = parseAndValidateCsv(csv);
    if (res.isValid && res.validBars.length > 0) {
      const newDataset: StockDataset = {
        id: `csv_${Date.now()}`,
        symbol: symbol.toUpperCase().trim(),
        name: name.trim() || `${symbol} Custom Dataset`,
        type: 'CUSTOM_CSV',
        description: desc.trim() || `User imported CSV with ${res.validBars.length} bars.`,
        bars: res.validBars,
        createdAt: Date.now()
      };
      setDatasets(prev => [newDataset, ...prev]);
      setSelectedDatasetId(newDataset.id);
    }
    return res;
  };

  const createSyntheticDataset = (
    symbol: string,
    name: string,
    desc: string,
    startPrice: number,
    vol: number,
    drift: number,
    numBars: number
  ) => {
    const newDs = generateSyntheticDataset(
      `synth_${Date.now()}`,
      symbol.toUpperCase().trim(),
      name,
      desc,
      startPrice,
      vol,
      drift,
      numBars,
      Date.now()
    );
    setDatasets(prev => [newDs, ...prev]);
    setSelectedDatasetId(newDs.id);
  };

  const deleteDataset = (id: string) => {
    if (datasets.length <= 1) return;
    setDatasets(prev => prev.filter(d => d.id !== id));
    if (selectedDatasetId === id) {
      setSelectedDatasetId(datasets.find(d => d.id !== id)!.id);
    }
  };

  const resetAllDemoData = () => {
    const fresh = getInitialDemoDatasets();
    setDatasets(fresh);
    setSelectedDatasetId(fresh[0].id);
    setPaperAccount({ startingCash: 10000, cash: 10000, realizedPnl: 0 });
    setPaperPositions([]);
    setPaperOrders([]);
    setLatestBacktest(null);
  };

  return (
    <TradingContext.Provider
      value={{
        datasets,
        selectedDataset,
        bars,
        strategyConfig,
        signals,
        fastMa,
        slowMa,
        rsi,
        latestSignal,
        backtestConfig,
        latestBacktest,
        isBacktesting,
        paperAccount,
        paperPositions,
        paperOrders,
        riskConfig,
        activeTab,
        setActiveTab,
        selectDataset,
        updateStrategy,
        updateBacktestConfig,
        updateRiskConfig,
        runBacktest,
        placePaperOrder,
        stepSimulationTrade,
        importCsvDataset,
        createSyntheticDataset,
        deleteDataset,
        resetAllDemoData
      }}
    >
      {children}
    </TradingContext.Provider>
  );
};

export const useTrading = () => {
  const context = useContext(TradingContext);
  if (!context) throw new Error('useTrading must be used within a TradingProvider');
  return context;
};
