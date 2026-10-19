import pytest
import pandas as pd
from backend.services.indicators import generate_signals
from backend.services.backtester import run_backtest_simulation

def test_backtest_execution():
    dates = [f"2026-01-{i:02d}" for i in range(1, 31)]
    closes = [100.0 + (i * 2.0) for i in range(30)]
    df = pd.DataFrame({
        "date": dates,
        "open": closes,
        "high": [c + 1.0 for c in closes],
        "low": [c - 1.0 for c in closes],
        "close": closes,
        "volume": [1500000] * 30
    })

    signals_df = generate_signals(df, "SMA_CROSSOVER", {"fast_period": 3, "slow_period": 7})
    results = run_backtest_simulation(
        df=signals_df,
        initial_capital=10000.0,
        fee_pct=0.1,
        slippage_pct=0.05
    )

    metrics = results["metrics"]
    assert metrics["initial_capital"] == 10000.0
    assert "total_return_pct" in metrics
    assert "max_drawdown_pct" in metrics
    assert "sharpe_ratio" in metrics
    assert len(results["equity_curve"]) == 30
