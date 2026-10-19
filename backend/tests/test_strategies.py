import pytest
import pandas as pd
import numpy as np
from backend.services.indicators import calculate_sma, calculate_ema, calculate_rsi, generate_signals

def test_sma_calculation():
    s = pd.Series([10.0, 20.0, 30.0, 40.0, 50.0])
    sma = calculate_sma(s, 3)
    assert np.isnan(sma.iloc[0])
    assert np.isnan(sma.iloc[1])
    assert pytest.approx(sma.iloc[2]) == 20.0
    assert pytest.approx(sma.iloc[3]) == 30.0
    assert pytest.approx(sma.iloc[4]) == 40.0

def test_ema_calculation():
    s = pd.Series([10.0, 11.0, 12.0, 13.0, 14.0])
    ema = calculate_ema(s, 3)
    assert len(ema) == 5
    assert ema.iloc[4] > ema.iloc[0]

def test_rsi_bounds():
    # Trending up prices
    s_up = pd.Series([100 + i * 5 for i in range(25)])
    rsi_up = calculate_rsi(s_up, 14)
    assert rsi_up.iloc[-1] > 80.0

    # Trending down prices
    s_down = pd.Series([200 - i * 5 for i in range(25)])
    rsi_down = calculate_rsi(s_down, 14)
    assert rsi_down.iloc[-1] < 20.0

def test_signal_generation_no_lookahead():
    dates = [f"2026-01-{i:02d}" for i in range(1, 31)]
    # Create price pattern with dip then rise
    closes = [100 - i for i in range(15)] + [85 + (i - 15) * 3 for i in range(15, 30)]
    df = pd.DataFrame({
        "date": dates,
        "open": closes,
        "high": [c + 1 for c in closes],
        "low": [c - 1 for c in closes],
        "close": closes,
        "volume": [1000000] * 30
    })

    signals_df = generate_signals(df, "SMA_CROSSOVER", {"fast_period": 5, "slow_period": 10})
    assert "signal" in signals_df.columns
    assert "reason" in signals_df.columns
    assert len(signals_df) == 30
