import React from 'react';
import { useTrading } from '../context/TradingContext';
import { TrendingUp, TrendingDown, Radio } from 'lucide-react';

export const TickerBar: React.FC = () => {
  const { datasets, selectedDataset, selectDataset } = useTrading();

  const marketTickers = [
    { symbol: 'AAPL', name: 'Apple Inc.', price: 182.40, change: 1.25, isUp: true, datasetId: 'demo_aapl' },
    { symbol: 'TSLA', name: 'Tesla Inc.', price: 218.50, change: -2.40, isUp: false, datasetId: 'demo_tsla' },
    { symbol: 'SPY', name: 'S&P 500 ETF', price: 496.20, change: 0.65, isUp: true, datasetId: 'demo_spy' },
    { symbol: 'BTC-USD', name: 'Bitcoin', price: 63450.00, change: 3.82, isUp: true, datasetId: 'demo_btc' },
    { symbol: 'NVDA', name: 'NVIDIA Corp', price: 128.50, change: 4.15, isUp: true },
    { symbol: 'GOOGL', name: 'Alphabet Inc.', price: 154.20, change: -0.45, isUp: false },
    { symbol: 'ETH-USD', name: 'Ethereum', price: 3450.00, change: 2.10, isUp: true },
    { symbol: 'MSFT', name: 'Microsoft Corp', price: 421.10, change: 0.85, isUp: true }
  ];

  return (
    <div className="h-9 bg-[#070C18] border-b border-[#162238] px-4 flex items-center justify-between overflow-x-auto text-[11px] font-mono shrink-0 select-none no-scrollbar">
      <div className="flex items-center space-x-2 mr-4 shrink-0 text-cyan-400 font-sans font-bold">
        <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
        <span className="text-[10px] uppercase tracking-wider">MARKET FEED</span>
      </div>

      <div className="flex items-center space-x-6 overflow-x-auto py-1">
        {marketTickers.map((ticker) => {
          const isSelected = selectedDataset.symbol.toUpperCase() === ticker.symbol.toUpperCase();
          const targetDataset = datasets.find(d => d.symbol.toUpperCase() === ticker.symbol.toUpperCase());

          return (
            <button
              key={ticker.symbol}
              onClick={() => {
                if (targetDataset) selectDataset(targetDataset.id);
              }}
              className={`flex items-center space-x-2 px-2 py-0.5 rounded transition-all shrink-0 ${
                isSelected
                  ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
                  : 'text-slate-300 hover:text-white hover:bg-[#101B38]'
              }`}
            >
              <span className="font-bold">{ticker.symbol}</span>
              <span className="text-white">${ticker.price.toLocaleString(undefined, { minimumFractionDigits: 2 })}</span>
              <span
                className={`flex items-center text-[10px] font-semibold ${
                  ticker.isUp ? 'text-emerald-400' : 'text-rose-400'
                }`}
              >
                {ticker.isUp ? <TrendingUp className="w-3 h-3 mr-0.5 inline" /> : <TrendingDown className="w-3 h-3 mr-0.5 inline" />}
                {ticker.isUp ? '+' : ''}{ticker.change.toFixed(2)}%
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
};
