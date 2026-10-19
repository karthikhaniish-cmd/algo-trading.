import React from 'react';
import { useTrading } from '../context/TradingContext';
import { CandlestickChart } from '../components/CandlestickChart';
import {
  TrendingUp,
  Wallet,
  Activity,
  ArrowUpRight,
  ArrowDownRight,
  Shield,
  Play,
  Briefcase
} from 'lucide-react';

export const DashboardView: React.FC = () => {
  const {
    selectedDataset,
    bars,
    fastMa,
    slowMa,
    rsi,
    latestSignal,
    strategyConfig,
    paperAccount,
    paperPositions,
    setActiveTab
  } = useTrading();

  const totalPositionsVal = paperPositions.reduce((acc, p) => acc + p.marketValue, 0);
  const netWorth = paperAccount.cash + totalPositionsVal;
  const totalPnl = netWorth - paperAccount.startingCash;
  const totalPnlPct = (totalPnl / paperAccount.startingCash) * 100;

  const lastBar = bars[bars.length - 1];
  const change = lastBar ? lastBar.close - lastBar.open : 0;
  const changePct = lastBar && lastBar.open > 0 ? (change / lastBar.open) * 100 : 0;
  const isUp = change >= 0;

  return (
    <div className="space-y-6">
      {/* Top Banner Notice */}
      <div className="bg-amber-500/10 border border-amber-500/30 rounded-xl p-3 flex items-center justify-between text-xs text-amber-300">
        <div className="flex items-center space-x-2">
          <Shield className="w-4 h-4 text-amber-400" />
          <span className="font-semibold">
            PAPER TRADING ENVIRONMENT: All orders and executions run on simulated virtual cash. No real-money brokerage connections.
          </span>
        </div>
        <span className="font-mono text-[10px] bg-amber-500/20 px-2 py-0.5 rounded">
          {selectedDataset.type}
        </span>
      </div>

      {/* Primary KPI Row */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Active Asset Card */}
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-2">
            <span>SELECTED ASSET</span>
            <Activity className="w-4 h-4 text-cyan-400" />
          </div>
          <p className="text-2xl font-black text-white">{selectedDataset.symbol}</p>
          <p className="text-xs text-slate-400 truncate">{selectedDataset.name}</p>
          <div className="mt-2 text-xs flex items-center space-x-1 font-bold">
            <span className={isUp ? 'text-emerald-400' : 'text-ruby-trading'}>
              ${lastBar?.close.toFixed(2)}
            </span>
            <span className={isUp ? 'text-emerald-400' : 'text-ruby-trading'}>
              ({isUp ? '+' : ''}{changePct.toFixed(2)}%)
            </span>
          </div>
        </div>

        {/* Strategy Signal Card */}
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-2">
            <span>STRATEGY SIGNAL</span>
            <TrendingUp className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="flex items-center space-x-2">
            <span
              className={`text-2xl font-black px-2.5 py-0.5 rounded ${
                latestSignal?.signal === 'BUY'
                  ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40'
                  : latestSignal?.signal === 'SELL'
                  ? 'bg-rose-500/20 text-rose-400 border border-rose-500/40'
                  : 'bg-slate-700/40 text-slate-300'
              }`}
            >
              {latestSignal?.signal || 'HOLD'}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-2 truncate">
            {strategyConfig.type.replace('_', ' ')}
          </p>
        </div>

        {/* Portfolio Net Worth */}
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-2">
            <span>NET PORTFOLIO VALUE</span>
            <Wallet className="w-4 h-4 text-cyan-400" />
          </div>
          <p className="text-2xl font-black text-white">${netWorth.toFixed(2)}</p>
          <p className="text-xs text-slate-400">Cash: ${paperAccount.cash.toFixed(2)}</p>
          <div className="mt-2 text-xs flex items-center space-x-1 font-bold text-slate-300">
            <span>Holdings: ${totalPositionsVal.toFixed(2)}</span>
          </div>
        </div>

        {/* Cumulative Profit/Loss */}
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-2">
            <span>SIMULATED P&L</span>
            {totalPnl >= 0 ? (
              <ArrowUpRight className="w-4 h-4 text-emerald-400" />
            ) : (
              <ArrowDownRight className="w-4 h-4 text-ruby-trading" />
            )}
          </div>
          <p
            className={`text-2xl font-black ${
              totalPnl >= 0 ? 'text-emerald-400' : 'text-ruby-trading'
            }`}
          >
            {totalPnl >= 0 ? '+' : ''}${totalPnl.toFixed(2)}
          </p>
          <p className="text-xs text-slate-400">
            Return: {totalPnl >= 0 ? '+' : ''}{totalPnlPct.toFixed(2)}%
          </p>
          <p className="text-[11px] text-slate-500 mt-2">
            Realized: ${paperAccount.realizedPnl.toFixed(2)}
          </p>
        </div>
      </div>

      {/* Main Candlestick Chart */}
      <CandlestickChart
        bars={bars}
        fastMa={fastMa}
        slowMa={slowMa}
        rsi={rsi}
        strategyType={strategyConfig.type}
      />

      {/* Explainable AI / Algorithmic Reason Box */}
      {latestSignal && (
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-bold text-cyan-400 flex items-center space-x-2">
              <span>ALGORITHMIC SIGNAL EXPLAINABILITY</span>
            </h3>
            <span className="text-xs text-slate-400">Timestamp: {latestSignal.date}</span>
          </div>
          <p className="text-sm text-slate-200 leading-relaxed bg-[#0B132B] p-3 rounded-lg border border-[#1B2C56]">
            {latestSignal.reason}
          </p>
          <p className="text-[11px] text-slate-500 mt-2">
            Notice: Orders trigger on the next available bar's Open price to enforce zero lookahead bias.
          </p>
        </div>
      )}

      {/* Quick Action Navigation Buttons */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <button
          onClick={() => setActiveTab('backtesting')}
          className="bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold p-4 rounded-xl flex items-center justify-center space-x-3 shadow-lg shadow-cyan-500/20 transition-all"
        >
          <Play className="w-5 h-5 fill-current" />
          <span>Launch Strategy Backtest Simulator</span>
        </button>

        <button
          onClick={() => setActiveTab('paper-trading')}
          className="bg-[#142244] hover:bg-[#1B2C56] border border-[#23386C] text-white font-bold p-4 rounded-xl flex items-center justify-center space-x-3 transition-all"
        >
          <Briefcase className="w-5 h-5 text-cyan-400" />
          <span>Open Paper Trading Terminal</span>
        </button>
      </div>
    </div>
  );
};
