from pydantic import BaseModel, Field
from typing import List, Optional

class PriceBarSchema(BaseModel):
    date: str
    open: float
    high: float
    low: float
    close: float
    volume: int

class DatasetCreateSchema(BaseModel):
    symbol: str
    name: str
    description: str
    bars: List[PriceBarSchema]

class DatasetOutSchema(BaseModel):
    id: str
    symbol: str
    name: str
    type: str
    description: str
    bar_count: int
    start_date: str
    end_date: str

class StrategyConfigSchema(BaseModel):
    strategy_type: str = "SMA_CROSSOVER"  # SMA_CROSSOVER, EMA_CROSSOVER, RSI_MEAN_REVERSION
    fast_period: int = 9
    slow_period: int = 21
    rsi_period: int = 14
    rsi_oversold: float = 30.0
    rsi_overbought: float = 70.0

class BacktestRequestSchema(BaseModel):
    dataset_id: str
    strategy: StrategyConfigSchema
    initial_capital: float = 10000.0
    fee_pct: float = 0.1
    slippage_pct: float = 0.05
    position_size_pct: float = 100.0
    stop_loss_pct: Optional[float] = 3.0
    take_profit_pct: Optional[float] = 6.0

class TradeRecordSchema(BaseModel):
    entry_date: str
    entry_price: float
    exit_date: str
    exit_price: float
    quantity: float
    pnl_dollar: float
    pnl_percent: float
    exit_reason: str
    duration_bars: int

class PerformanceMetricsSchema(BaseModel):
    initial_capital: float
    final_capital: float
    total_return_pct: float
    buy_and_hold_return_pct: float
    total_trades: int
    winning_trades: int
    losing_trades: int
    win_rate_pct: float
    profit_factor: float
    max_drawdown_pct: float
    sharpe_ratio: float

class BacktestResponseSchema(BaseModel):
    id: str
    dataset_name: str
    symbol: str
    strategy_name: str
    metrics: PerformanceMetricsSchema
    trades: List[TradeRecordSchema]

class PaperOrderRequestSchema(BaseModel):
    symbol: str
    side: str  # BUY, SELL
    quantity: float
    price: float
    strategy: str = "Manual"

class PaperOrderOutSchema(BaseModel):
    id: str
    timestamp: int
    symbol: str
    side: str
    quantity: float
    price: float
    strategy: str
    status: str
    realized_pnl: float
    note: str

class PaperAccountOutSchema(BaseModel):
    cash: float
    positions_value: float
    net_worth: float
    realized_pnl: float
