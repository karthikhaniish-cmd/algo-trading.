import pandas as pd
import numpy as np
from typing import List, Dict, Any, Optional

def calculate_sma(series: pd.Series, period: int) -> pd.Series:
    """Calculates Simple Moving Average using Pandas rolling."""
    return series.rolling(window=period).mean()

def calculate_ema(series: pd.Series, period: int) -> pd.Series:
    """Calculates Exponential Moving Average using Pandas ewm."""
    return series.ewm(span=period, adjust=False).mean()

def calculate_rsi(series: pd.Series, period: int = 14) -> pd.Series:
    """
    Calculates Relative Strength Index (RSI) using Wilder's smoothing technique.
    RSI = 100 - (100 / (1 + RS))
    """
    delta = series.diff()
    gain = delta.where(delta > 0, 0.0)
    loss = -delta.where(delta < 0, 0.0)

    avg_gain = gain.ewm(alpha=1/period, min_periods=period, adjust=False).mean()
    avg_loss = loss.ewm(alpha=1/period, min_periods=period, adjust=False).mean()

    rs = avg_gain / avg_loss
    rsi = 100 - (100 / (1 + rs))
    return rsi

def generate_signals(df: pd.DataFrame, strategy_type: str, params: Dict[str, Any]) -> pd.DataFrame:
    """
    Applies algorithmic rules on price dataframe with zero lookahead bias.
    Signals: BUY (1), SELL (-1), HOLD (0).
    """
    df = df.copy()
    df["signal"] = 0
    df["reason"] = "Hold"

    if strategy_type == "SMA_CROSSOVER":
        fast_p = params.get("fast_period", 9)
        slow_p = params.get("slow_period", 21)
        df["fast_ma"] = calculate_sma(df["close"], fast_p)
        df["slow_ma"] = calculate_sma(df["close"], slow_p)

        # Golden Cross & Death Cross
        golden_cross = (df["fast_ma"] > df["slow_ma"]) & (df["fast_ma"].shift(1) <= df["slow_ma"].shift(1))
        death_cross = (df["fast_ma"] < df["slow_ma"]) & (df["fast_ma"].shift(1) >= df["slow_ma"].shift(1))

        df.loc[golden_cross, "signal"] = 1
        df.loc[golden_cross, "reason"] = f"SMA({fast_p}) crossed above SMA({slow_p}) [Golden Cross]"
        df.loc[death_cross, "signal"] = -1
        df.loc[death_cross, "reason"] = f"SMA({fast_p}) crossed below SMA({slow_p}) [Death Cross]"

    elif strategy_type == "EMA_CROSSOVER":
        fast_p = params.get("fast_period", 9)
        slow_p = params.get("slow_period", 21)
        df["fast_ma"] = calculate_ema(df["close"], fast_p)
        df["slow_ma"] = calculate_ema(df["close"], slow_p)

        golden_cross = (df["fast_ma"] > df["slow_ma"]) & (df["fast_ma"].shift(1) <= df["slow_ma"].shift(1))
        death_cross = (df["fast_ma"] < df["slow_ma"]) & (df["fast_ma"].shift(1) >= df["slow_ma"].shift(1))

        df.loc[golden_cross, "signal"] = 1
        df.loc[golden_cross, "reason"] = f"EMA({fast_p}) crossed above EMA({slow_p})"
        df.loc[death_cross, "signal"] = -1
        df.loc[death_cross, "reason"] = f"EMA({fast_p}) crossed below EMA({slow_p})"

    elif strategy_type == "RSI_MEAN_REVERSION":
        rsi_p = params.get("rsi_period", 14)
        oversold = params.get("rsi_oversold", 30.0)
        overbought = params.get("rsi_overbought", 70.0)
        df["rsi"] = calculate_rsi(df["close"], rsi_p)

        oversold_bounce = (df["rsi"] > oversold) & (df["rsi"].shift(1) <= oversold)
        overbought_exit = (df["rsi"] < overbought) & (df["rsi"].shift(1) >= overbought)

        df.loc[oversold_bounce, "signal"] = 1
        df.loc[oversold_bounce, "reason"] = f"RSI crossed above {oversold} (Oversold Mean Reversion)"
        df.loc[overbought_exit, "signal"] = -1
        df.loc[overbought_exit, "reason"] = f"RSI crossed below {overbought} (Overbought Mean Reversion Exit)"

    return df
