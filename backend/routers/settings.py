from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from backend.database import get_db, Base, engine
from backend.models import PaperOrderModel, PaperPositionModel, PaperAccountModel, BacktestRecordModel, DatasetModel, PriceBarModel

router = APIRouter(prefix="/api/settings", tags=["Settings"])

@router.post("/reset-demo-data")
def reset_demo_data(db: Session = Depends(get_db)):
    db.query(PaperOrderModel).delete()
    db.query(PaperPositionModel).delete()
    db.query(BacktestRecordModel).delete()

    acct = db.query(PaperAccountModel).filter(PaperAccountModel.id == 1).first()
    if acct:
        acct.cash = 10000.0
        acct.realized_pnl = 0.0
    else:
        acct = PaperAccountModel(id=1, starting_cash=10000.0, cash=10000.0, realized_pnl=0.0)
        db.add(acct)

    db.commit()
    return {"status": "success", "message": "Demo data reset to factory state."}
