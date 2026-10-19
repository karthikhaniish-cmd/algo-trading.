export type DatasetType = 'SYNTHETIC' | 'HISTORICAL' | 'CUSTOM_CSV';

export interface PriceBar {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface StockDataset {
  id: string;
  symbol: string;
  name: string;
  type: DatasetType;
  description: string;
  bars: PriceBar[];
  createdAt: number;
}

export type StrategyType = 'SMA_CROSSOVER' | 'EMA_CROSSOVER' | 'RSI_MEAN_REVERSION';

export interface StrategyConfig {
  type: StrategyType;
  fastPeriod: number;
  slowPeriod: number;
  rsiPeriod: number;
  rsiOversold: number;
  rsiOverbought: number;
}

export type SignalAction = 'BUY' | 'SELL' | 'HOLD';

export interface TradingSignal {
  index: number;
  date: string;
  price: number;
  signal: SignalAction;
  indicator1?: number | null;
  indicator2?: number | null;
  reason: string;
}

export interface BacktestConfig {
  initialCapital: number;
  feePercent: number;
  slippagePercent: number;
  positionSizePercent: number;
  stopLossPercent?: number;
  takeProfitPercent?: number;
}

export interface TradeRecord {
  id: string;
  entryDate: string;
  entryPrice: number;
  exitDate: string;
  exitPrice: number;
  quantity: number;
  pnlDollar: number;
  pnlPercent: number;
  exitReason: string;
  durationBars: number;
}

export interface EquityPoint {
  date: string;
  equity: number;
  cash: number;
  drawdownPct: number;
  benchmarkEquity: number;
}

export interface PerformanceMetrics {
  initialCapital: number;
  finalCapital: number;
  totalReturnPct: number;
  buyAndHoldReturnPct: number;
  totalTrades: number;
  winningTrades: number;
  losingTrades: number;
  winRatePct: number;
  profitFactor: number;
  maxDrawdownPct: number;
  sharpeRatio: number;
}

export interface BacktestResult {
  id: string;
  datasetName: string;
  symbol: string;
  strategyName: string;
  config: BacktestConfig;
  strategyConfig: StrategyConfig;
  metrics: PerformanceMetrics;
  trades: TradeRecord[];
  equityCurve: EquityPoint[];
  timestamp: number;
}

export interface PaperAccount {
  startingCash: number;
  cash: number;
  realizedPnl: number;
}

export interface PaperPosition {
  symbol: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  marketValue: number;
  unrealizedPnl: number;
  unrealizedPnlPct: number;
}

export interface PaperOrder {
  id: string;
  timestamp: number;
  symbol: string;
  side: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  strategy: string;
  status: 'FILLED' | 'REJECTED';
  realizedPnl: number;
  note: string;
}

export interface RiskConfig {
  maxPositionSizePct: number;
  maxPortfolioExposurePct: number;
  defaultStopLossPct: number;
  defaultTakeProfitPct: number;
}
