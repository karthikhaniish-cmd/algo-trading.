import React from 'react';
import { useTrading } from '../context/TradingContext';
import { StrategyType } from '../types/trading';
import { GitCompare, Sliders, CheckCircle2, Info } from 'lucide-react';

export const StrategiesView: React.FC = () => {
  const {
    strategyConfig,
    updateStrategy,
    signals,
    selectedDataset
  } = useTrading();

  const strategies: { type: StrategyType; title: string; desc: string }[] = [
    {
      type: 'SMA_CROSSOVER',
      title: 'Simple Moving Average (SMA) Crossover',
      desc: 'Buy on Golden Cross (Fast SMA crosses above Slow SMA). Sell on Death Cross.'
    },
    {
      type: 'EMA_CROSSOVER',
      title: 'Exponential Moving Average (EMA) Crossover',
      desc: 'Weight-adjusted crossover giving higher priority to recent price momentum.'
    },
    {
      type: 'RSI_MEAN_REVERSION',
      title: 'Relative Strength Index (RSI) Reversion',
      desc: 'Buys when RSI rebounds from oversold territory and sells when overbought.'
    }
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-black text-white">Trading Strategies & Technical Indicators</h2>
        <p className="text-xs text-slate-400">
          Configure algorithmic indicator rules, optimize lookback parameters, and review mathematical signal explainability.
        </p>
      </div>

      {/* Strategy Selection Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {strategies.map((s) => {
          const isSelected = strategyConfig.type === s.type;
          return (
            <div
              key={s.type}
              onClick={() => updateStrategy({ type: s.type })}
              className={`p-5 rounded-xl border cursor-pointer transition-all ${
                isSelected
                  ? 'bg-[#142244] border-cyan-400 shadow-md shadow-cyan-500/10'
                  : 'bg-[#101B38] border-[#1B2C56] hover:border-[#2F4885]'
              }`}
            >
              <div className="flex items-center justify-between mb-2">
                <span className={`text-sm font-bold ${isSelected ? 'text-cyan-400' : 'text-white'}`}>
                  {s.title}
                </span>
                {isSelected && <CheckCircle2 className="w-4 h-4 text-cyan-400" />}
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">{s.desc}</p>
            </div>
          );
        })}
      </div>

      {/* Parameter Tuning Panel */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-6">
        <div className="flex items-center space-x-2 text-cyan-400 mb-4">
          <Sliders className="w-5 h-5" />
          <h3 className="text-sm font-bold uppercase tracking-wide">
            Parameter Tuning for {strategyConfig.type.replace('_', ' ')}
          </h3>
        </div>

        {strategyConfig.type === 'SMA_CROSSOVER' || strategyConfig.type === 'EMA_CROSSOVER' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <div className="flex justify-between text-xs font-bold text-slate-300">
                <span>Fast Period (N1)</span>
                <span className="text-cyan-400 font-mono">{strategyConfig.fastPeriod} bars</span>
              </div>
              <input
                type="range"
                min={3}
                max={50}
                value={strategyConfig.fastPeriod}
                onChange={(e) => updateStrategy({ fastPeriod: Number(e.target.value) })}
                className="w-full accent-cyan-400 cursor-pointer"
              />
              <p className="text-[11px] text-slate-500">
                Short-term momentum baseline. Typical ranges: 5 to 20 bars.
              </p>
            </div>

            <div className="space-y-2">
              <div className="flex justify-between text-xs font-bold text-slate-300">
                <span>Slow Period (N2)</span>
                <span className="text-amber-400 font-mono">{strategyConfig.slowPeriod} bars</span>
              </div>
              <input
                type="range"
                min={10}
                max={200}
                value={strategyConfig.slowPeriod}
                onChange={(e) => updateStrategy({ slowPeriod: Number(e.target.value) })}
                className="w-full accent-amber-400 cursor-pointer"
              />
              <p className="text-[11px] text-slate-500">
                Long-term trend filter. Typical ranges: 20 to 200 bars.
              </p>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="space-y-2">
              <div className="flex justify-between text-xs font-bold text-slate-300">
                <span>RSI Lookback Period</span>
                <span className="text-cyan-400 font-mono">{strategyConfig.rsiPeriod} bars</span>
              </div>
              <input
                type="range"
                min={5}
                max={30}
                value={strategyConfig.rsiPeriod}
                onChange={(e) => updateStrategy({ rsiPeriod: Number(e.target.value) })}
                className="w-full accent-cyan-400 cursor-pointer"
              />
            </div>

            <div className="space-y-2">
              <div className="flex justify-between text-xs font-bold text-slate-300">
                <span>Oversold Threshold (Buy)</span>
                <span className="text-emerald-400 font-mono">{strategyConfig.rsiOversold}</span>
              </div>
              <input
                type="range"
                min={15}
                max={45}
                value={strategyConfig.rsiOversold}
                onChange={(e) => updateStrategy({ rsiOversold: Number(e.target.value) })}
                className="w-full accent-emerald-400 cursor-pointer"
              />
            </div>

            <div className="space-y-2">
              <div className="flex justify-between text-xs font-bold text-slate-300">
                <span>Overbought Threshold (Sell)</span>
                <span className="text-ruby-trading font-mono">{strategyConfig.rsiOverbought}</span>
              </div>
              <input
                type="range"
                min={55}
                max={85}
                value={strategyConfig.rsiOverbought}
                onChange={(e) => updateStrategy({ rsiOverbought: Number(e.target.value) })}
                className="w-full accent-ruby-trading cursor-pointer"
              />
            </div>
          </div>
        )}
      </div>

      {/* Generated Signals Audit Trail */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
        <h3 className="text-sm font-bold text-white mb-3">
          Historical Signal Log on {selectedDataset.symbol} ({signals.length} bars)
        </h3>
        <div className="overflow-x-auto max-h-72">
          <table className="w-full text-xs text-left">
            <thead className="text-[11px] text-slate-400 uppercase bg-[#0B132B] sticky top-0 border-b border-[#1B2C56]">
              <tr>
                <th className="py-2.5 px-3">Date</th>
                <th className="py-2.5 px-3">Price</th>
                <th className="py-2.5 px-3">Signal</th>
                <th className="py-2.5 px-3">Algorithmic Reasoning</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1B2C56] text-slate-300">
              {signals.slice(-20).reverse().map((sig, idx) => (
                <tr key={idx} className="hover:bg-[#142244]/50">
                  <td className="py-2.5 px-3 font-mono text-white font-semibold">{sig.date}</td>
                  <td className="py-2.5 px-3 font-mono">${sig.price.toFixed(2)}</td>
                  <td className="py-2.5 px-3">
                    <span
                      className={`text-[10px] font-extrabold px-2 py-0.5 rounded ${
                        sig.signal === 'BUY'
                          ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                          : sig.signal === 'SELL'
                          ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                          : 'bg-slate-700/40 text-slate-400'
                      }`}
                    >
                      {sig.signal}
                    </span>
                  </td>
                  <td className="py-2.5 px-3 text-slate-300">{sig.reason}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
