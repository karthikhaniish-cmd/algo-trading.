# Algorithmic Trading System
**College Final-Year Capstone Project**  
*A High-Performance Quantitative Analysis, Strategy Backtesting & Paper Trading Platform*

---

## 1. Project Overview
The **Algorithmic Trading System** is a complete, production-ready financial engineering application designed to model, test, and simulate algorithmic stock trading strategies without risking real capital. The platform strictly operates in **Historical Simulation & Paper Trading mode** and provides end-to-end tooling across 8 specialized modules.

### Key Capabilities
- **Technical Indicator Engine:** Simple Moving Average (SMA), Exponential Moving Average (EMA), and Wilder's Relative Strength Index (RSI).
- **Realistic Backtesting Engine:** Simulates historical executions on the **next available bar's open price** to rigorously eliminate look-ahead bias, accounting for transaction fees, slippage, and stop-loss/take-profit rules.
- **Virtual Paper Trading:** Complete virtual cash ledger, position tracking, mark-to-market valuations, realized/unrealized P&L, and safety safeguards.
- **Risk Management:** Pre-trade validation enforcing maximum position size caps, overall portfolio exposure ceilings, and risk-to-reward ratios.
- **Data Ingestion & Integrity:** Validates custom CSV uploads for OHLC integrity, chronological dates, and positive volumes; bundles realistic synthetic Geometric Brownian Motion data generators.

---

## 2. Technology Stack & Architecture

```
                  ┌──────────────────────────────────────────────┐
                  │      Algorithmic Trading Platform UI         │
                  │  (Android Jetpack Compose M3 / Dark Navy)    │
                  └──────────────────────┬───────────────────────┘
                                         │
                   StateFlow & Coroutines│
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │       Domain & Quantitative Engine           │
                  │  • Technical Indicators (SMA, EMA, RSI)      │
                  │  • Next-Bar Backtesting Simulation Engine    │
                  │  • Paper Trading Virtual Portfolio Manager   │
                  │  • Pre-Trade Risk Management Validator      │
                  └──────────────────────┬───────────────────────┘
                                         │
                         Room ORM / SQL  │
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │        Local Persistence Layer               │
                  │  • SQLite via Room Database                  │
                  │  • Datasets, Price Bars, Orders, Positions   │
                  └──────────────────────────────────────────────┘
                                         │
                    Companion Services   │
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │          Python FastAPI Backend              │
                  │  • Pandas / NumPy Data Analysis              │
                  │  • Pytest Automated Test Suite               │
                  └──────────────────────────────────────────────┘
```

- **Frontend / Client:** Jetpack Compose Material 3 with customized Dark Navy financial palette, interactive touch-scrubbing Canvas candlestick charts, volume bars, RSI sub-panels, and equity curves.
- **Database:** SQLite with Android Room ORM, using structured tables for Datasets, Price Bars, Paper Orders, Positions, Account Balances, and Backtest runs.
- **Backend Service (Python):** FastAPI with Pydantic schemas, SQLAlchemy ORM, Pandas, and NumPy.
- **Testing:** Local JVM unit tests with Robolectric and Pytest for strategy calculations, boundary conditions, and risk rules.

---

## 3. Mathematical Formulas & Algorithmic Rules

### 1. Simple Moving Average (SMA)
$$\text{SMA}_t(N) = \frac{1}{N} \sum_{i=0}^{N-1} P_{t-i}$$
- **Golden Cross (BUY):** Fast SMA crosses above Slow SMA.
- **Death Cross (SELL):** Fast SMA crosses below Slow SMA.

### 2. Exponential Moving Average (EMA)
$$\alpha = \frac{2}{N + 1}$$
$$\text{EMA}_t(N) = \alpha \cdot P_t + (1 - \alpha) \cdot \text{EMA}_{t-1}(N)$$
Initialized with the SMA of the first $N$ bars.

### 3. Relative Strength Index (RSI - Wilder's Smoothing)
$$\text{RS} = \frac{\text{Smoothed Average Gain}}{\text{Smoothed Average Loss}}$$
$$\text{RSI} = 100 - \left( \frac{100}{1 + \text{RS}} \right)$$
- **Oversold (BUY):** RSI drops below configurable threshold (default 30) and rebounds, anticipating mean reversion.
- **Overbought (SELL):** RSI exceeds configurable threshold (default 70), indicating over-extended prices.

### 4. Annualized Sharpe Ratio
$$\text{Sharpe} = \frac{\mathbb{E}[R_p - R_f]}{\sigma_p} \times \sqrt{252}$$
Where $R_f$ is the daily risk-free rate (assumed 2.0% annually / 252 days).

### 5. Maximum Drawdown (MDD)
$$\text{Drawdown}_t = \frac{\text{Peak Equity}_t - \text{Equity}_t}{\text{Peak Equity}_t} \times 100\%$$
$$\text{MDD} = \max_{t} (\text{Drawdown}_t)$$

---

## 4. Module Specifications

### Module 1: Dashboard
- Displays active stock symbol, latest close, daily dollar change, percent change, and data timestamp.
- Touch-interactive candlestick chart with high/low wicks, volume bars, moving average overlays, and RSI pane.
- Real-time strategy signal (BUY / SELL / HOLD) with transparent algorithmic explanations.
- Virtual portfolio summary (Cash, Positions value, Net worth, Total P&L).

### Module 2: Market Data
- Dataset selector with historical inspection (bar count, range high/low, average volume).
- CSV upload/paste with rigorous validation:
  - Header inspection (Date, Open, High, Low, Close, Volume).
  - Date normalization (YYYY-MM-DD, MM/DD/YYYY).
  - OHLC integrity check: $\text{High} \ge \max(\text{Open}, \text{Close})$ and $\text{Low} \le \min(\text{Open}, \text{Close})$.
  - Duplicate date detection and non-numeric value rejection.
- Synthetic Market Generator using Geometric Brownian Motion with stochastic wicks and drift.

### Module 3: Trading Strategies
- Selection between SMA Crossover, EMA Crossover, and RSI Mean Reversion.
- Interactive parameter sliders (Fast Period, Slow Period, RSI Period, Oversold/Overbought levels).
- Step-by-step explainability logs for every historical bar.

### Module 4: Backtesting Engine
- User-defined starting capital, fee % (e.g. 0.1%), slippage % (e.g. 0.05%), stop-loss %, take-profit %.
- Execution on **Bar $i+1$'s Open price** after Bar $i$'s close signal (zero look-ahead bias).
- Metric scorecard: Total Return %, Buy & Hold benchmark %, Win Rate %, Total Trades, Profit Factor, Max Drawdown %, and Annualized Sharpe Ratio.
- Visual Equity Curve & Drawdown chart.

### Module 5: Paper Trading
- Dedicated "PAPER TRADING (VIRTUAL MONEY)" status banner.
- Virtual portfolio ledger tracking cash, active positions, average cost basis, and mark-to-market P&L.
- Manual order submission with automatic pre-trade risk evaluation.
- "Auto Step" simulation runner to walk forward through signals bar-by-bar.

### Module 6: Risk Management
- Configurable Max Position Size (default 25% of portfolio).
- Max Portfolio Exposure Ceiling (default 80%, reserving 20% cash).
- Stop-Loss and Take-Profit automated exits.
- Pre-trade interactive order simulator.

### Module 7: Trade History and Reports
- Searchable and filterable log of all executed simulated orders (by symbol, side, strategy).
- CSV export capability via Android Native Share sheet and clipboard copy.
- Strategy comparison metrics.

### Module 8: Settings
- Default simulation variables (initial capital, fees, slippage).
- Factory reset demo data with safety confirmation dialog.
- College capstone architectural summary.

---

## 5. Sample API Requests & Responses (FastAPI)

### Health Check
```http
GET /api/health
```
```json
{
  "status": "healthy",
  "service": "Algorithmic Trading System API",
  "mode": "Simulation / Paper Trading Only",
  "version": "1.0.0"
}
```

### Run Backtest Simulation
```http
POST /api/backtest/run
Content-Type: application/json

{
  "dataset_id": "demo_aapl",
  "strategy": {
    "strategy_type": "SMA_CROSSOVER",
    "fast_period": 9,
    "slow_period": 21
  },
  "initial_capital": 10000.0,
  "fee_pct": 0.1,
  "slippage_pct": 0.05,
  "stop_loss_pct": 3.0,
  "take_profit_pct": 6.0
}
```
```json
{
  "id": "e4b6c31a-514d-45f8-8bb0-264663e29f3d",
  "dataset_name": "Apple Inc. (Synthetic)",
  "symbol": "AAPL",
  "strategy_name": "SMA_CROSSOVER",
  "metrics": {
    "initial_capital": 10000.0,
    "final_capital": 11420.50,
    "total_return_pct": 14.21,
    "buy_and_hold_return_pct": 8.35,
    "total_trades": 8,
    "winning_trades": 6,
    "losing_trades": 2,
    "win_rate_pct": 75.0,
    "profit_factor": 2.45,
    "max_drawdown_pct": 4.12,
    "sharpe_ratio": 1.62
  },
  "trades": [
    {
      "entry_date": "2026-01-15",
      "entry_price": 161.58,
      "exit_date": "2026-02-02",
      "exit_price": 171.27,
      "quantity": 61.88,
      "pnl_dollar": 599.45,
      "pnl_percent": 5.99,
      "exit_reason": "Take Profit (+6.0%)",
      "duration_bars": 12
    }
  ]
}
```

---

## 6. How to Run the Project

### Running the Android Applet
1. Open the project in Google AI Studio or Android Studio.
2. Build and launch on the emulator or device:
   ```bash
   gradle assembleDebug
   ```
3. Run the automated local test suite:
   ```bash
   gradle :app:testDebugUnitTest
   ```

### Running the Python FastAPI Backend & Pytest
```bash
# 1. Install dependencies
pip install -r requirements.txt

# 2. Run the FastAPI development server
uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload

# 3. Access Interactive Swagger API Docs
# Open http://localhost:8000/docs in your browser

# 4. Run Pytest automated test suite
pytest backend/tests/ -v
```

---

## 7. Verification Checklist & Test Results
- [x] **Technical Indicators:** SMA, EMA, and Wilder's RSI unit tests verified (`testSmaCalculation`, `testEmaCalculation`, `testRsiCalculationBounds`).
- [x] **Zero Look-ahead Bias:** Verified execution on next bar open prices without future bar leakage (`testBacktestEngineNoLookaheadBias`).
- [x] **CSV Ingestion & Integrity:** Validated column headers, invalid OHLC rejection ($H < L$), and duplicate date rejection (`testCsvParserRejectsInvalidOhlc`, `testCsvParserRejectsDuplicateDates`).
- [x] **Risk Policy Enforcement:** Verified position size capping and portfolio cash protection (`testRiskEngineEnforcesLimits`).
- [x] **Paper Trading Ledger:** Realized and unrealized P&L tracking, average entry price recalculation.
- [x] **Database Persistence:** SQLite Room ORM schema with entities, DAO, and singleton database.
- [x] **Automated Build & Execution:** `gradle :app:testDebugUnitTest` executed with **BUILD SUCCESSFUL** across all 33 tasks.
