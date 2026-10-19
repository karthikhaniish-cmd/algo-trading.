import React from 'react';
import { ChevronDown, ShieldCheck, DollarSign } from 'lucide-react';
import { useTrading } from '../context/TradingContext';

export const Navbar: React.FC = () => {
  const {
    datasets,
    selectedDataset,
    selectDataset,
    bars,
    latestSignal,
    paperAccount,
    paperPositions
  } = useTrading();

  const lastBar = bars[bars.length - 1];
  const change = lastBar ? lastBar.close - lastBar.open : 0;
  const changePct = lastBar && lastBar.open > 0 ? (change / lastBar.open) * 100 : 0;
  const isUp = change >= 0;

  const totalPositionsVal = paperPositions.reduce((acc, p) => acc + p.marketValue, 0);
  const netWorth = paperAccount.cash + totalPositionsVal;

  return (
    <header className="h-16 bg-[#0B132B] border-b border-[#1B2C56] px-6 flex items-center justify-between z-10 shrink-0">
      {/* Asset Selector */}
      <div className="flex items-center space-x-6">
        <div className="relative">
          <select
            value={selectedDataset.id}
            onChange={(e) => selectDataset(e.target.value)}
            className="appearance-none bg-[#142244] text-white font-bold text-sm pl-4 pr-10 py-2 rounded-lg border border-[#23386C] hover:border-cyan-400 focus:outline-none focus:border-cyan-400 cursor-pointer transition-colors"
          >
            {datasets.map((ds) => (
              <option key={ds.id} value={ds.id} className="bg-[#0B132B] text-slate-200">
                {ds.symbol} - {ds.name}
              </option>
            ))}
          </select>
          <ChevronDown className="w-4 h-4 text-cyan-400 absolute right-3 top-3 pointer-events-none" />
        </div>

        {/* Dataset Type Badge */}
        <span
          className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
            selectedDataset.type === 'SYNTHETIC'
              ? 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30'
              : selectedDataset.type === 'CUSTOM_CSV'
              ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
              : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
          }`}
        >
          {selectedDataset.type === 'SYNTHETIC' ? 'SYNTHETIC DATA' : selectedDataset.type}
        </span>

        {/* Live Quote & Change */}
        {lastBar && (
          <div className="flex items-baseline space-x-3">
            <span className="text-xl font-extrabold text-white">
              ${lastBar.close.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
            <span
              className={`text-xs font-bold px-2 py-0.5 rounded ${
                isUp ? 'text-emerald-400 bg-emerald-500/10' : 'text-ruby-trading bg-ruby-trading/10'
              }`}
            >
              {isUp ? '+' : ''}${change.toFixed(2)} ({isUp ? '+' : ''}{changePct.toFixed(2)}%)
            </span>
          </div>
        )}
      </div>

      {/* Right Controls: Signal Badge & Paper Cash */}
      <div className="flex items-center space-x-4">
        {/* Signal Badge */}
        {latestSignal && (
          <div className="flex items-center space-x-2 bg-[#142244] border border-[#23386C] px-3 py-1.5 rounded-lg">
            <span className="text-[11px] text-slate-400 font-medium">STRATEGY:</span>
            <span
              className={`text-xs font-extrabold px-2 py-0.5 rounded ${
                latestSignal.signal === 'BUY'
                  ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                  : latestSignal.signal === 'SELL'
                  ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                  : 'bg-slate-700/50 text-slate-300'
              }`}
            >
              {latestSignal.signal}
            </span>
          </div>
        )}

        {/* Paper Portfolio Stat */}
        <div className="flex items-center space-x-2 bg-[#101B38] border border-[#1B2C56] px-3 py-1.5 rounded-lg">
          <DollarSign className="w-4 h-4 text-emerald-400" />
          <div className="text-right">
            <p className="text-[9px] text-slate-400 font-semibold uppercase leading-none">Net Worth</p>
            <p className="text-xs font-extrabold text-white leading-tight">
              ${netWorth.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
          </div>
        </div>

        {/* Mode Indicator */}
        <div className="flex items-center space-x-1.5 text-xs text-amber-400 bg-amber-500/10 border border-amber-500/30 px-3 py-1.5 rounded-lg">
          <ShieldCheck className="w-4 h-4" />
          <span className="font-bold text-[11px]">PAPER TRADING ONLY</span>
        </div>
      </div>
    </header>
  );
};
