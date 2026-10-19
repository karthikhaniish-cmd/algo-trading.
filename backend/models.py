from sqlalchemy import Column, Integer, String, Float, Boolean, BigInteger, ForeignKey, Text
from sqlalchemy.orm import relationship
from backend.database import Base
import time

class DatasetModel(Base):
    __tablename__ = "datasets"

    id = Column(String, primary_key=True, index=True)
    symbol = Column(String, index=True)
    name = Column(String)
    type = Column(String)  # SYNTHETIC, HISTORICAL, CUSTOM_CSV
    description = Column(String)
    created_at = Column(BigInteger, default=lambda: int(time.time() * 1000))

    bars = relationship("PriceBarModel", back_populates="dataset", cascade="all, delete-orphan")

class PriceBarModel(Base):
    __tablename__ = "price_bars"

    id = Column(Integer, primary_key=True, autoincrement=True)
    dataset_id = Column(String, ForeignKey("datasets.id"), index=True)
    date = Column(String, index=True)
    open = Column(Float)
    high = Column(Float)
    low = Column(Float)
    close = Column(Float)
    volume = Column(BigInteger)

    dataset = relationship("DatasetModel", back_populates="bars")

class PaperOrderModel(Base):
    __tablename__ = "paper_orders"

    id = Column(String, primary_key=True)
    timestamp = Column(BigInteger, default=lambda: int(time.time() * 1000))
    symbol = Column(String, index=True)
    side = Column(String)  # BUY, SELL
    quantity = Column(Float)
    price = Column(Float)
    strategy = Column(String)
    status = Column(String)  # FILLED, REJECTED
    realized_pnl = Column(Float, default=0.0)
    note = Column(String, default="")

class PaperPositionModel(Base):
    __tablename__ = "paper_positions"

    symbol = Column(String, primary_key=True)
    quantity = Column(Float)
    average_price = Column(Float)
    current_price = Column(Float)

class PaperAccountModel(Base):
    __tablename__ = "paper_account"

    id = Column(Integer, primary_key=True, default=1)
    starting_cash = Column(Float, default=10000.0)
    cash = Column(Float, default=10000.0)
    realized_pnl = Column(Float, default=0.0)

class BacktestRecordModel(Base):
    __tablename__ = "backtest_records"

    id = Column(String, primary_key=True)
    dataset_name = Column(String)
    symbol = Column(String)
    strategy_name = Column(String)
    initial_capital = Column(Float)
    final_capital = Column(Float)
    total_return_pct = Column(Float)
    buy_and_hold_return_pct = Column(Float)
    win_rate_pct = Column(Float)
    total_trades = Column(Integer)
    max_drawdown_pct = Column(Float)
    sharpe_ratio = Column(Float)
    details_json = Column(Text, default="{}")
    timestamp = Column(BigInteger, default=lambda: int(time.time() * 1000))
