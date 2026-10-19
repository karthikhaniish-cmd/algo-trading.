from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
import pandas as pd
from backend.database import get_db
from backend.models import PriceBarModel
from backend.schemas import StrategyConfigSchema
from backend.services.indicators import generate_signals

router = APIRouter(prefix="/api/strategies", tags=["Strategies"])

@router.post("/calculate/{dataset_id}")
def calculate_strategy_signals(
    dataset_id: str,
    config: StrategyConfigSchema,
    db: Session = Depends(get_db)
):
    bars = db.query(PriceBarModel).filter(PriceBarModel.dataset_id == dataset_id).order_by(PriceBarModel.date.asc()).all()
    if not bars:
        raise HTTPException(status_code=404, detail="Dataset not found")

    df = pd.DataFrame([{
        "date": b.date,
        "open": b.open,
        "high": b.high,
        "low": b.low,
        "close": b.close,
        "volume": b.volume
    } for b in bars])

    params = config.dict()
    df_signals = generate_signals(df, config.strategy_type, params)

    signals_out = []
    for i, row in df_signals.iterrows():
        signals_out.append({
            "date": row["date"],
            "close": row["close"],
            "signal": "BUY" if row["signal"] == 1 else ("SELL" if row["signal"] == -1 else "HOLD"),
            "reason": row["reason"],
            "fast_ma": None if pd.isna(row.get("fast_ma")) else float(row.get("fast_ma")),
            "slow_ma": None if pd.isna(row.get("slow_ma")) else float(row.get("slow_ma")),
            "rsi": None if pd.isna(row.get("rsi")) else float(row.get("rsi"))
        })

    return {
        "strategy": config.strategy_type,
        "dataset_id": dataset_id,
        "signals": signals_out
    }
