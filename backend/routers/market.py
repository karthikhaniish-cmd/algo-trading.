from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sqlalchemy.orm import Session
from typing import List
import io
import pandas as pd
import uuid

from backend.database import get_db
from backend.models import DatasetModel, PriceBarModel
from backend.schemas import DatasetOutSchema, PriceBarSchema

router = APIRouter(prefix="/api/market", tags=["Market Data"])

@router.get("/datasets", response_model=List[DatasetOutSchema])
def list_datasets(db: Session = Depends(get_db)):
    datasets = db.query(DatasetModel).all()
    results = []
    for d in datasets:
        bars = db.query(PriceBarModel).filter(PriceBarModel.dataset_id == d.id).order_by(PriceBarModel.date.asc()).all()
        results.append(DatasetOutSchema(
            id=d.id,
            symbol=d.symbol,
            name=d.name,
            type=d.type,
            description=d.description,
            bar_count=len(bars),
            start_date=bars[0].date if bars else "",
            end_date=bars[-1].date if bars else ""
        ))
    return results

@router.get("/datasets/{dataset_id}/bars", response_model=List[PriceBarSchema])
def get_dataset_bars(dataset_id: str, db: Session = Depends(get_db)):
    bars = db.query(PriceBarModel).filter(PriceBarModel.dataset_id == dataset_id).order_by(PriceBarModel.date.asc()).all()
    if not bars:
        raise HTTPException(status_code=404, detail="Dataset not found or contains no bars")
    return [PriceBarSchema(date=b.date, open=b.open, high=b.high, low=b.low, close=b.close, volume=b.volume) for b in bars]

@router.post("/upload-csv")
async def upload_csv(
    symbol: str,
    name: str,
    description: str = "",
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    content = await file.read()
    try:
        df = pd.read_csv(io.BytesIO(content))
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Failed to parse CSV: {str(e)}")

    req_cols = ["date", "open", "high", "low", "close", "volume"]
    df.columns = [c.strip().lower() for c in df.columns]

    for c in req_cols:
        if c not in df.columns:
            raise HTTPException(status_code=422, detail=f"Missing required CSV column '{c}'")

    dataset_id = "csv_" + str(uuid.uuid4())[:8]
    dataset = DatasetModel(
        id=dataset_id,
        symbol=symbol.upper().strip(),
        name=name,
        type="CUSTOM_CSV",
        description=description or f"User uploaded CSV with {len(df)} records"
    )
    db.add(dataset)

    for _, row in df.iterrows():
        bar = PriceBarModel(
            dataset_id=dataset_id,
            date=str(row["date"]),
            open=float(row["open"]),
            high=float(row["high"]),
            low=float(row["low"]),
            close=float(row["close"]),
            volume=int(row["volume"])
        )
        db.add(bar)

    db.commit()
    return {"status": "success", "dataset_id": dataset_id, "bars_imported": len(df)}
