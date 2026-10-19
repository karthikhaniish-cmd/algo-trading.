from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
import uuid

from backend.database import get_db
from backend.models import PaperOrderModel, PaperPositionModel, PaperAccountModel
from backend.schemas import PaperOrderRequestSchema, PaperOrderOutSchema, PaperAccountOutSchema

router = APIRouter(prefix="/api/paper-trading", tags=["Paper Trading"])

def get_or_create_account(db: Session) -> PaperAccountModel:
    acct = db.query(PaperAccountModel).filter(PaperAccountModel.id == 1).first()
    if not acct:
        acct = PaperAccountModel(id=1, starting_cash=10000.0, cash=10000.0, realized_pnl=0.0)
        db.add(acct)
        db.commit()
        db.refresh(acct)
    return acct

@router.get("/portfolio", response_model=PaperAccountOutSchema)
def get_portfolio(db: Session = Depends(get_db)):
    acct = get_or_create_account(db)
    positions = db.query(PaperPositionModel).filter(PaperPositionModel.quantity > 0).all()
    pos_val = sum(p.quantity * p.current_price for p in positions)
    return {
        "cash": acct.cash,
        "positions_value": pos_val,
        "net_worth": acct.cash + pos_val,
        "realized_pnl": acct.realized_pnl
    }

@router.post("/order", response_model=PaperOrderOutSchema)
def execute_order(
    req: PaperOrderRequestSchema,
    db: Session = Depends(get_db)
):
    acct = get_or_create_account(db)
    side = req.side.upper().strip()
    cost = req.quantity * req.price
    order_id = str(uuid.uuid4())

    if side == "BUY":
        if cost > acct.cash:
            order = PaperOrderModel(
                id=order_id,
                symbol=req.symbol.upper(),
                side="BUY",
                quantity=req.quantity,
                price=req.price,
                strategy=req.strategy,
                status="REJECTED",
                note="Insufficient virtual cash balance."
            )
            db.add(order)
            db.commit()
            raise HTTPException(status_code=400, detail="Insufficient virtual cash balance")

        acct.cash -= cost
        pos = db.query(PaperPositionModel).filter(PaperPositionModel.symbol == req.symbol.upper()).first()
        if not pos:
            pos = PaperPositionModel(
                symbol=req.symbol.upper(),
                quantity=req.quantity,
                average_price=req.price,
                current_price=req.price
            )
            db.add(pos)
        else:
            total_qty = pos.quantity + req.quantity
            tot_cost = (pos.quantity * pos.average_price) + cost
            pos.average_price = tot_cost / total_qty
            pos.quantity = total_qty
            pos.current_price = req.price

        order = PaperOrderModel(
            id=order_id,
            symbol=req.symbol.upper(),
            side="BUY",
            quantity=req.quantity,
            price=req.price,
            strategy=req.strategy,
            status="FILLED",
            realized_pnl=0.0,
            note="Simulated Buy executed successfully."
        )
        db.add(order)
        db.commit()
        db.refresh(order)
        return order

    elif side == "SELL":
        pos = db.query(PaperPositionModel).filter(PaperPositionModel.symbol == req.symbol.upper()).first()
        if not pos or pos.quantity < req.quantity:
            order = PaperOrderModel(
                id=order_id,
                symbol=req.symbol.upper(),
                side="SELL",
                quantity=req.quantity,
                price=req.price,
                strategy=req.strategy,
                status="REJECTED",
                note="Insufficient shares held for sell."
            )
            db.add(order)
            db.commit()
            raise HTTPException(status_code=400, detail="Insufficient shares held for sell order")

        realized = (req.price - pos.average_price) * req.quantity
        acct.cash += cost
        acct.realized_pnl += realized

        pos.quantity -= req.quantity
        pos.current_price = req.price
        if pos.quantity <= 0.0001:
            db.delete(pos)

        order = PaperOrderModel(
            id=order_id,
            symbol=req.symbol.upper(),
            side="SELL",
            quantity=req.quantity,
            price=req.price,
            strategy=req.strategy,
            status="FILLED",
            realized_pnl=realized,
            note="Simulated Sell executed successfully."
        )
        db.add(order)
        db.commit()
        db.refresh(order)
        return order

    else:
        raise HTTPException(status_code=400, detail="Invalid order side: must be BUY or SELL")

@router.get("/orders", response_model=List[PaperOrderOutSchema])
def list_orders(db: Session = Depends(get_db)):
    return db.query(PaperOrderModel).order_by(PaperOrderModel.timestamp.desc()).all()
