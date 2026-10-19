from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
import pandas as pd
import uuid
import json

from backend.database import get_db
from backend.models import DatasetModel, PriceBarModel, BacktestRecordModel
from backend.schemas import BacktestRequestSchema, BacktestResponseSchema
from backend.services.indicators import generate_signals
from backend.services.backtester import run_backtest_simulation

router = APIRouter(prefix="/api/backtest", tags=["Backtesting"])

@router.post("/run", response_model=BacktestResponseSchema)
def run_backtest(
    req: BacktestRequestSchema,
    db: Session = Depends(get_db)
):
    dataset = db.query(DatasetModel).filter(DatasetModel.id == req.dataset_id).first()
    if not dataset:
        raise HTTPException(status_code=404, detail="Dataset not found")

    bars = db.query(PriceBarModel).filter(PriceBarModel.dataset_id == req.dataset_id).order_by(PriceBarModel.date.asc()).all()
    if len(bars) < 2:
        raise HTTPException(status_code=400, detail="Insufficient price bars for backtesting")

    df = pd.DataFrame([{
        "date": b.date,
        "open": b.open,
        "high": b.high,
        "low": b.low,
        "close": b.close,
        "volume": b.volume
    } for b in bars])

    # 1. Generate signals
    df_signals = generate_signals(df, req.strategy.strategy_type, req.strategy.dict())

    # 2. Run simulation with next-bar execution
    results = run_backtest_simulation(
        df=df_signals,
        initial_capital=req.initial_capital,
        fee_pct=req.fee_pct,
        slippage_pct=req.slippage_pct,
        stop_loss_pct=req.stop_loss_pct,
        take_profit_pct=req.take_profit_pct
    )

    backtest_id = str(uuid.uuid4())
    m = results["metrics"]

    record = BacktestRecordModel(
        id=backtest_id,
        dataset_name=dataset.name,
        symbol=dataset.symbol,
        strategy_name=req.strategy.strategy_type,
        initial_capital=m["initial_capital"],
        final_capital=m["final_capital"],
        total_return_pct=m["total_return_pct"],
        buy_and_hold_return_pct=m["buy_and_hold_return_pct"],
        win_rate_pct=m["win_rate_pct"],
        total_trades=m["total_trades"],
        max_drawdown_pct=m["max_drawdown_pct"],
        sharpe_ratio=m["sharpe_ratio"],
        details_json=json.dumps(results["trades"])
    )
    db.add(record)
    db.commit()

    return {
        "id": backtest_id,
        "dataset_name": dataset.name,
        "symbol": dataset.symbol,
        "strategy_name": req.strategy.strategy_type,
        "metrics": m,
        "trades": results["trades"]
    }
