import React, { useState } from 'react';
import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ReferenceLine
} from 'recharts';
import { PriceBar } from '../types/trading';

interface ChartProps {
  bars: PriceBar[];
  fastMa: (number | null)[];
  slowMa: (number | null)[];
  rsi: (number | null)[];
  strategyType: string;
}

export const CandlestickChart: React.FC<ChartProps> = ({
  bars,
  fastMa,
  slowMa,
  rsi,
  strategyType
}) => {
  const [showRsi, setShowRsi] = useState(false);

  const chartData = bars.map((b, i) => ({
    date: b.date,
    open: b.open,
    high: b.high,
    low: b.low,
    close: b.close,
    volume: b.volume,
    isUp: b.close >= b.open,
    fast: fastMa[i] || undefined,
    slow: slowMa[i] || undefined,
    rsiVal: rsi[i] || undefined
  }));

  const minPrice = Math.min(...bars.map(b => b.low)) * 0.98;
  const maxPrice = Math.max(...bars.map(b => b.high)) * 1.02;

  return (
    <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5 shadow-lg">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-4">
          <span className="text-sm font-bold text-white">Price Action & Indicators</span>
          <div className="flex items-center space-x-3 text-xs">
            <div className="flex items-center space-x-1">
              <span className="w-2.5 h-0.5 bg-cyan-400 inline-block"></span>
              <span className="text-cyan-400 font-medium">Fast MA</span>
            </div>
            <div className="flex items-center space-x-1">
              <span className="w-2.5 h-0.5 bg-amber-400 inline-block"></span>
              <span className="text-amber-400 font-medium">Slow MA</span>
            </div>
          </div>
        </div>

        <button
          onClick={() => setShowRsi(!showRsi)}
          className={`text-xs font-bold px-3 py-1.5 rounded-lg border transition-all ${
            showRsi
              ? 'bg-cyan-500/20 text-cyan-400 border-cyan-500/40'
              : 'bg-[#142244] text-slate-300 border-[#23386C] hover:border-cyan-400'
          }`}
        >
          {showRsi ? 'Hide RSI Sub-chart' : 'Show RSI Oscillator'}
        </button>
      </div>

      {/* Main Price Chart */}
      <div className="h-96 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#1B2C56" vertical={false} />
            <XAxis dataKey="date" stroke="#64748B" tick={{ fontSize: 10 }} tickLine={false} />
            <YAxis domain={[minPrice, maxPrice]} stroke="#64748B" tick={{ fontSize: 10 }} tickLine={false} />
            <Tooltip content={<CustomTooltip />} />
            {/* Price Line */}
            <Line
              type="monotone"
              dataKey="close"
              stroke="#38BDF8"
              strokeWidth={2}
              dot={false}
              isAnimationActive={false}
            />
            {/* Fast Moving Average */}
            <Line
              type="monotone"
              dataKey="fast"
              stroke="#00E5FF"
              strokeWidth={2}
              dot={false}
              isAnimationActive={false}
            />
            {/* Slow Moving Average */}
            <Line
              type="monotone"
              dataKey="slow"
              stroke="#FFD600"
              strokeWidth={2}
              dot={false}
              isAnimationActive={false}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>

      {/* Volume Chart */}
      <div className="h-16 w-full mt-2">
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={chartData} margin={{ top: 0, right: 10, left: -20, bottom: 0 }}>
            <YAxis hide={true} />
            <Bar dataKey="volume" fill="#0284C7" opacity={0.4} />
          </ComposedChart>
        </ResponsiveContainer>
      </div>

      {/* RSI Sub-Chart */}
      {showRsi && (
        <div className="mt-4 pt-4 border-t border-[#1B2C56]">
          <div className="flex items-center justify-between text-xs mb-2">
            <span className="font-bold text-slate-300">RSI Oscillator (14)</span>
            <div className="flex space-x-3 text-[10px]">
              <span className="text-ruby-trading font-semibold">Overbought: 70</span>
              <span className="text-emerald-trading font-semibold">Oversold: 30</span>
            </div>
          </div>
          <div className="h-28 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={chartData} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1B2C56" vertical={false} />
                <YAxis domain={[0, 100]} ticks={[30, 50, 70]} stroke="#64748B" tick={{ fontSize: 9 }} />
                <ReferenceLine y={70} stroke="#FF5252" strokeDasharray="3 3" opacity={0.6} />
                <ReferenceLine y={50} stroke="#64748B" strokeDasharray="3 3" opacity={0.3} />
                <ReferenceLine y={30} stroke="#00E676" strokeDasharray="3 3" opacity={0.6} />
                <Line
                  type="monotone"
                  dataKey="rsiVal"
                  stroke="#38BDF8"
                  strokeWidth={2}
                  dot={false}
                  isAnimationActive={false}
                />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </div>
  );
};

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    const change = data.close - data.open;
    const isUp = change >= 0;

    return (
      <div className="bg-[#070C18] border border-[#23386C] p-3 rounded-lg shadow-xl text-xs space-y-1">
        <p className="font-bold text-slate-200 border-b border-[#1B2C56] pb-1">{label}</p>
        <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-slate-300">
          <div>Open: <span className="font-semibold text-white">${data.open.toFixed(2)}</span></div>
          <div>Close: <span className={`font-semibold ${isUp ? 'text-emerald-400' : 'text-ruby-trading'}`}>${data.close.toFixed(2)}</span></div>
          <div>High: <span className="font-semibold text-emerald-400">${data.high.toFixed(2)}</span></div>
          <div>Low: <span className="font-semibold text-ruby-trading">${data.low.toFixed(2)}</span></div>
        </div>
        <div className="pt-1 border-t border-[#1B2C56] text-[11px] text-slate-400 flex justify-between">
          <span>Vol: {(data.volume / 1000).toFixed(0)}k</span>
          {data.fast && <span className="text-cyan-400">Fast: {data.fast}</span>}
          {data.slow && <span className="text-amber-400">Slow: {data.slow}</span>}
          {data.rsiVal && <span className="text-emerald-400">RSI: {data.rsiVal}</span>}
        </div>
      </div>
    );
  }
  return null;
};
