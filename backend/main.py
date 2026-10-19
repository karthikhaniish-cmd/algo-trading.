from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from backend.database import Base, engine
from backend.routers import market, strategies, backtest, papertrade, reports, settings

# Create database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Algorithmic Trading System API",
    description="Quantitative trading analytics, technical indicators (SMA, EMA, RSI), next-bar backtesting engine, and paper trading simulator.",
    version="1.0.0"
)

# Enable CORS for frontend integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routers
app.include_router(market.router)
app.include_router(strategies.router)
app.include_router(backtest.router)
app.include_router(papertrade.router)
app.include_router(reports.router)
app.include_router(settings.router)

@app.get("/api/health")
def health_check():
    return {
        "status": "healthy",
        "service": "Algorithmic Trading System API",
        "mode": "Simulation / Paper Trading Only",
        "version": "1.0.0"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.main:app", host="0.0.0.0", port=8000, reload=True)
