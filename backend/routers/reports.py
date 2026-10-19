from fastapi import APIRouter, Depends, Response
from sqlalchemy.orm import Session
import io
import csv

from backend.database import get_db
from backend.models import PaperOrderModel, BacktestRecordModel

router = APIRouter(prefix="/api/reports", tags=["Reports"])

@router.get("/export-csv")
def export_trade_history_csv(db: Session = Depends(get_db)):
    orders = db.query(PaperOrderModel).order_by(PaperOrderModel.timestamp.desc()).all()
    output = io.StringIO()
    writer = csv.writer(output)

    writer.writerow(["Order ID", "Timestamp", "Symbol", "Side", "Quantity", "Price", "Strategy", "Status", "Realized PnL", "Note"])
    for o in orders:
        writer.writerow([o.id, o.timestamp, o.symbol, o.side, o.quantity, o.price, o.strategy, o.status, o.realized_pnl, o.note])

    csv_data = output.getvalue()
    return Response(
        content=csv_data,
        media_type="text/csv",
        headers={"Content-Disposition": "attachment; filename=trade_history.csv"}
    )

@router.get("/backtests")
def list_backtest_records(db: Session = Depends(get_db)):
    return db.query(BacktestRecordModel).order_by(BacktestRecordModel.timestamp.desc()).all()
