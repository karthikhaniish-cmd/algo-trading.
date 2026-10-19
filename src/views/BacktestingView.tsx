import React from 'react';
import { useTrading } from '../context/TradingContext';
import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  Area
} from 'recharts';
import { Play, TrendingUp, AlertCircle, Award, Target, Shield } from 'lucide-react';

export const BacktestingView: React.FC = () => {
  const {
    selectedDataset,
    strategyConfig,
    backtestConfig,
    updateBacktestConfig,
    latestBacktest,
    runBacktest,
    isBacktesting
  } = useTrading();

  const m = latestBacktest?.metrics;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-black text-white">Strategy Backtesting Engine</h2>
        <p className="text-xs text-slate-400">
          Evaluates strategy profitability against historical price bars with next-bar execution, transaction costs, and slippage to eliminate lookahead bias.
        </p>
      </div>

      {/* Configuration Form Card */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-6">
        <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
          <span>SIMULATION PARAMETERS</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Starting Capital ($)</label>
            <input
              type="number"
              value={backtestConfig.initialCapital}
              onChange={(e) => updateBacktestConfig({ initialCapital: Number(e.target.value) })}
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400 font-mono"
            />
          </div>

          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Fee per Trade (%)</label>
            <input
              type="number"
              step="0.05"
              value={backtestConfig.feePercent}
              onChange={(e) => updateBacktestConfig({ feePercent: Number(e.target.value) })}
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400 font-mono"
            />
          </div>

          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Slippage (%)</label>
            <input
              type="number"
              step="0.01"
              value={backtestConfig.slippagePercent}
              onChange={(e) => updateBacktestConfig({ slippagePercent: Number(e.target.value) })}
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400 font-mono"
            />
          </div>

          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Stop Loss (%)</label>
            <input
              type="number"
              step="0.5"
              value={backtestConfig.stopLossPercent || ''}
              onChange={(e) => updateBacktestConfig({ stopLossPercent: e.target.value ? Number(e.target.value) : undefined })}
              placeholder="e.g. 3.0"
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400 font-mono"
            />
          </div>

          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Take Profit (%)</label>
            <input
              type="number"
              step="0.5"
              value={backtestConfig.takeProfitPercent || ''}
              onChange={(e) => updateBacktestConfig({ takeProfitPercent: e.target.value ? Number(e.target.value) : undefined })}
              placeholder="e.g. 6.0"
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400 font-mono"
            />
          </div>
        </div>

        <button
          onClick={runBacktest}
          disabled={isBacktesting}
          className="w-full mt-5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold py-3 rounded-lg text-sm flex items-center justify-center space-x-2 transition-all shadow-lg shadow-cyan-500/20"
        >
          <Play className="w-4 h-4 fill-current" />
          <span>{isBacktesting ? 'Running Simulation...' : `Run Backtest on ${selectedDataset.symbol}`}</span>
        </button>
      </div>

      {/* Results Dashboard */}
      {m ? (
        <div className="space-y-6">
          {/* Scorecard Grid */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
              <span className="text-xs font-bold text-slate-400 block mb-1">STRATEGY RETURN</span>
              <p className={`text-2xl font-black ${m.totalReturnPct >= 0 ? 'text-emerald-400' : 'text-ruby-trading'}`}>
                {m.totalReturnPct >= 0 ? '+' : ''}{m.totalReturnPct.toFixed(2)}%
              </p>
              <p className="text-[11px] text-slate-400 mt-1">Final: ${m.finalCapital.toFixed(2)}</p>
            </div>

            <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
              <span className="text-xs font-bold text-slate-400 block mb-1">BENCHMARK (BUY & HOLD)</span>
              <p className={`text-2xl font-black ${m.buyAndHoldReturnPct >= 0 ? 'text-emerald-400' : 'text-ruby-trading'}`}>
                {m.buyAndHoldReturnPct >= 0 ? '+' : ''}{m.buyAndHoldReturnPct.toFixed(2)}%
              </p>
              <p className="text-[11px] text-slate-400 mt-1">100% equity buy & hold</p>
            </div>

            <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
              <span className="text-xs font-bold text-slate-400 block mb-1">WIN RATE</span>
              <p className="text-2xl font-black text-cyan-400">{m.winRatePct.toFixed(1)}%</p>
              <p className="text-[11px] text-slate-400 mt-1">
                {m.winningTrades} wins / {m.losingTrades} losses ({m.totalTrades} total)
              </p>
            </div>

            <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
              <span className="text-xs font-bold text-slate-400 block mb-1">PROFIT FACTOR</span>
              <p className="text-2xl font-black text-white">{m.profitFactor.toFixed(2)}</p>
              <p className="text-[11px] text-slate-400 mt-1">Gross Gains / Gross Losses</p>
            </div>

            <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
              <span className="text-xs font-bold text-slate-400 block mb-1">MAX DRAWDOWN</span>
              <p className="text-2xl font-black text-ruby-trading">-{m.maxDrawdownPct.toFixed(2)}%</p>
              <p className="text-[11px] text-slate-400 mt-1">Peak-to-trough decline</p>
            </div>

            <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
              <span className="text-xs font-bold text-slate-400 block mb-1">SHARPE RATIO</span>
              <p className="text-2xl font-black text-amber-400">{m.sharpeRatio.toFixed(2)}</p>
              <p className="text-[11px] text-slate-400 mt-1">Annualized risk-adjusted return</p>
            </div>
          </div>

          {/* Equity Curve Chart */}
          <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-bold text-white">Equity Curve vs Buy & Hold Benchmark</h3>
              <div className="flex items-center space-x-4 text-xs font-medium">
                <div className="flex items-center space-x-1">
                  <span className="w-2.5 h-0.5 bg-cyan-400 inline-block"></span>
                  <span className="text-cyan-400">Strategy</span>
                </div>
                <div className="flex items-center space-x-1">
                  <span className="w-2.5 h-0.5 bg-slate-400 inline-block"></span>
                  <span className="text-slate-400">Benchmark</span>
                </div>
              </div>
            </div>

            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <ComposedChart data={latestBacktest.equityCurve} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1B2C56" vertical={false} />
                  <XAxis dataKey="date" stroke="#64748B" tick={{ fontSize: 10 }} />
                  <YAxis stroke="#64748B" tick={{ fontSize: 10 }} domain={['auto', 'auto']} />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#070C18', borderColor: '#23386C', fontSize: '11px' }}
                  />
                  <Line type="monotone" dataKey="benchmarkEquity" stroke="#64748B" strokeDasharray="3 3" dot={false} />
                  <Line type="monotone" dataKey="equity" stroke="#00E5FF" strokeWidth={2} dot={false} />
                </ComposedChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Trade Log */}
          <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
            <h3 className="text-sm font-bold text-white mb-3">
              Trade Execution Log ({latestBacktest.trades.length} trades)
            </h3>
            <div className="overflow-x-auto max-h-72">
              <table className="w-full text-xs text-left">
                <thead className="text-[11px] text-slate-400 uppercase bg-[#0B132B] sticky top-0 border-b border-[#1B2C56]">
                  <tr>
                    <th className="py-2.5 px-3">Entry</th>
                    <th className="py-2.5 px-3">Exit</th>
                    <th className="py-2.5 px-3">Shares</th>
                    <th className="py-2.5 px-3">P&L ($)</th>
                    <th className="py-2.5 px-3">P&L (%)</th>
                    <th className="py-2.5 px-3">Exit Reason</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#1B2C56] text-slate-300 font-mono">
                  {latestBacktest.trades.slice().reverse().map((t, idx) => (
                    <tr key={idx} className="hover:bg-[#142244]/50">
                      <td className="py-2 px-3">
                        <span className="text-white">{t.entryDate}</span> @ ${t.entryPrice.toFixed(2)}
                      </td>
                      <td className="py-2 px-3">
                        <span className="text-white">{t.exitDate}</span> @ ${t.exitPrice.toFixed(2)}
                      </td>
                      <td className="py-2 px-3">{t.quantity}</td>
                      <td className={`py-2 px-3 font-bold ${t.pnlDollar >= 0 ? 'text-emerald-400' : 'text-ruby-trading'}`}>
                        {t.pnlDollar >= 0 ? '+' : ''}${t.pnlDollar.toFixed(2)}
                      </td>
                      <td className={`py-2 px-3 font-bold ${t.pnlPercent >= 0 ? 'text-emerald-400' : 'text-ruby-trading'}`}>
                        {t.pnlPercent >= 0 ? '+' : ''}{t.pnlPercent.toFixed(2)}%
                      </td>
                      <td className="py-2 px-3 text-slate-400 font-sans">{t.exitReason}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      ) : (
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-8 text-center text-slate-400 text-xs">
          <AlertCircle className="w-8 h-8 text-cyan-400 mx-auto mb-2 opacity-50" />
          <p>No backtest executed yet for current session. Click "Run Backtest" above to simulate.</p>
        </div>
      )}
    </div>
  );
};
