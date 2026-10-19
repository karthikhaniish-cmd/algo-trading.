import math
import numpy as np
import pandas as pd
from typing import Dict, Any, List

def run_backtest_simulation(
    df: pd.DataFrame,
    initial_capital: float = 10000.0,
    fee_pct: float = 0.1,
    slippage_pct: float = 0.05,
    stop_loss_pct: float = None,
    take_profit_pct: float = None
) -> Dict[str, Any]:
    cash = initial_capital
    shares = 0.0
    entry_price = 0.0
    entry_date = ""
    entry_idx = 0

    fee_multiplier = fee_pct / 100.0
    slippage_multiplier = slippage_pct / 100.0

    trades = []
    equity_curve = []
    peak_equity = initial_capital
    max_drawdown = 0.0

    for i in range(len(df)):
        row = df.iloc[i]
        date = str(row["date"])
        open_price = float(row["open"])
        high_price = float(row["high"])
        low_price = float(row["low"])
        close_price = float(row["close"])

        # Check pending signals from PREVIOUS bar to prevent lookahead bias
        if i > 0:
            prev_row = df.iloc[i - 1]
            prev_signal = int(prev_row.get("signal", 0))

            # Stop-loss and take-profit checks
            if shares > 0:
                hit_sl = stop_loss_pct is not None and low_price <= entry_price * (1.0 - stop_loss_pct / 100.0)
                hit_tp = take_profit_pct is not None and high_price >= entry_price * (1.0 + take_profit_pct / 100.0)

                if hit_sl:
                    exit_price = entry_price * (1.0 - stop_loss_pct / 100.0)
                    gross = shares * exit_price
                    fee = gross * fee_multiplier
                    net = gross - fee
                    pnl_dollar = net - (shares * entry_price)
                    pnl_pct = ((exit_price - entry_price) / entry_price) * 100.0
                    cash += net
                    trades.append({
                        "entry_date": entry_date,
                        "entry_price": entry_price,
                        "exit_date": date,
                        "exit_price": exit_price,
                        "quantity": shares,
                        "pnl_dollar": pnl_dollar,
                        "pnl_percent": pnl_pct,
                        "exit_reason": f"Stop Loss (-{stop_loss_pct}%)",
                        "duration_bars": i - entry_idx
                    })
                    shares = 0.0

                elif hit_tp:
                    exit_price = entry_price * (1.0 + take_profit_pct / 100.0)
                    gross = shares * exit_price
                    fee = gross * fee_multiplier
                    net = gross - fee
                    pnl_dollar = net - (shares * entry_price)
                    pnl_pct = ((exit_price - entry_price) / entry_price) * 100.0
                    cash += net
                    trades.append({
                        "entry_date": entry_date,
                        "entry_price": entry_price,
                        "exit_date": date,
                        "exit_price": exit_price,
                        "quantity": shares,
                        "pnl_dollar": pnl_dollar,
                        "pnl_percent": pnl_pct,
                        "exit_reason": f"Take Profit (+{take_profit_pct}%)",
                        "duration_bars": i - entry_idx
                    })
                    shares = 0.0

            # Execute previous bar signals on current bar Open + slippage
            if prev_signal == 1 and shares == 0:
                exec_price = open_price * (1.0 + slippage_multiplier)
                trade_capital = cash * 0.99
                fee = trade_capital * fee_multiplier
                net_invest = trade_capital - fee
                bought_shares = net_invest / exec_price
                if bought_shares > 0:
                    cash -= trade_capital
                    shares = bought_shares
                    entry_price = exec_price
                    entry_date = date
                    entry_idx = i

            elif prev_signal == -1 and shares > 0:
                exec_price = open_price * (1.0 - slippage_multiplier)
                gross = shares * exec_price
                fee = gross * fee_multiplier
                net = gross - fee
                pnl_dollar = net - (shares * entry_price)
                pnl_pct = ((exec_price - entry_price) / entry_price) * 100.0
                cash += net
                trades.append({
                    "entry_date": entry_date,
                    "entry_price": entry_price,
                    "exit_date": date,
                    "exit_price": exec_price,
                    "quantity": shares,
                    "pnl_dollar": pnl_dollar,
                    "pnl_percent": pnl_pct,
                    "exit_reason": "Strategy Signal Exit",
                    "duration_bars": i - entry_idx
                })
                shares = 0.0

        current_val = cash + (shares * close_price)
        peak_equity = max(peak_equity, current_val)
        drawdown = ((peak_equity - current_val) / peak_equity) * 100.0 if peak_equity > 0 else 0.0
        max_drawdown = max(max_drawdown, drawdown)

        equity_curve.append({
            "date": date,
            "equity": current_val,
            "cash": cash,
            "drawdown_pct": drawdown
        })

    # Liquidate remaining shares on final bar
    if shares > 0:
        last_row = df.iloc[-1]
        exec_price = float(last_row["close"]) * (1.0 - slippage_multiplier)
        gross = shares * exec_price
        fee = gross * fee_multiplier
        net = gross - fee
        pnl_dollar = net - (shares * entry_price)
        pnl_pct = ((exec_price - entry_price) / entry_price) * 100.0
        cash += net
        trades.append({
            "entry_date": entry_date,
            "entry_price": entry_price,
            "exit_date": str(last_row["date"]),
            "exit_price": exec_price,
            "quantity": shares,
            "pnl_dollar": pnl_dollar,
            "pnl_percent": pnl_pct,
            "exit_reason": "End of Backtest Window",
            "duration_bars": len(df) - 1 - entry_idx
        })
        shares = 0.0

    final_capital = cash
    total_return_pct = ((final_capital - initial_capital) / initial_capital) * 100.0

    first_price = float(df.iloc[0]["open"])
    last_price = float(df.iloc[-1]["close"])
    buy_hold_return_pct = ((last_price - first_price) / first_price) * 100.0 if first_price > 0 else 0.0

    total_trades = len(trades)
    winning = [t for t in trades if t["pnl_dollar"] > 0]
    losing = [t for t in trades if t["pnl_dollar"] < 0]
    win_rate = (len(winning) / total_trades * 100.0) if total_trades > 0 else 0.0

    gross_profit = sum(t["pnl_dollar"] for t in winning)
    gross_loss = abs(sum(t["pnl_dollar"] for t in losing))
    profit_factor = (gross_profit / gross_loss) if gross_loss > 0 else (99.9 if gross_profit > 0 else 0.0)

    # Sharpe ratio
    daily_equities = [p["equity"] for p in equity_curve]
    daily_returns = pd.Series(daily_equities).pct_change().dropna()
    mean_ret = daily_returns.mean()
    std_ret = daily_returns.std()
    sharpe = float((mean_ret / std_ret) * math.sqrt(252)) if std_ret and std_ret > 0 else 0.0

    metrics = {
        "initial_capital": initial_capital,
        "final_capital": final_capital,
        "total_return_pct": total_return_pct,
        "buy_and_hold_return_pct": buy_hold_return_pct,
        "total_trades": total_trades,
        "winning_trades": len(winning),
        "losing_trades": len(losing),
        "win_rate_pct": win_rate,
        "profit_factor": profit_factor,
        "max_drawdown_pct": max_drawdown,
        "sharpe_ratio": sharpe
    }

    return {
        "metrics": metrics,
        "trades": trades,
        "equity_curve": equity_curve
    }
