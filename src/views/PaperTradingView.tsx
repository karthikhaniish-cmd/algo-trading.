import React, { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import { Wallet, ShoppingCart, FastForward, CheckCircle, AlertTriangle } from 'lucide-react';

export const PaperTradingView: React.FC = () => {
  const {
    selectedDataset,
    bars,
    paperAccount,
    paperPositions,
    paperOrders,
    placePaperOrder,
    stepSimulationTrade
  } = useTrading();

  const currentPrice = bars[bars.length - 1]?.close || 100;
  const [side, setSide] = useState<'BUY' | 'SELL'>('BUY');
  const [quantity, setQuantity] = useState<number>(10);
  const [feedback, setFeedback] = useState<{ success: boolean; message: string } | null>(null);

  const totalPositionsVal = paperPositions.reduce((acc, p) => acc + p.marketValue, 0);
  const netWorth = paperAccount.cash + totalPositionsVal;

  const handleOrder = () => {
    const res = placePaperOrder(selectedDataset.symbol, side, quantity, currentPrice);
    setFeedback(res);
  };

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="bg-amber-500/10 border border-amber-500/30 rounded-xl p-3 flex items-center justify-between text-xs text-amber-300">
        <span className="font-bold">
          PAPER TRADING TERMINAL — Virtual Funds Only. All positions are simulated.
        </span>
        <button
          onClick={stepSimulationTrade}
          className="bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold px-3 py-1 rounded text-xs flex items-center space-x-1.5 transition-all"
        >
          <FastForward className="w-3.5 h-3.5" />
          <span>Auto-Execute Strategy Signal</span>
        </button>
      </div>

      {/* Account Balances Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
          <span className="text-xs font-bold text-slate-400 block mb-1">AVAILABLE CASH</span>
          <p className="text-2xl font-black text-white">${paperAccount.cash.toFixed(2)}</p>
          <p className="text-[11px] text-slate-400 mt-1">Virtual Purchasing Power</p>
        </div>

        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
          <span className="text-xs font-bold text-slate-400 block mb-1">OPEN POSITIONS VALUE</span>
          <p className="text-2xl font-black text-white">${totalPositionsVal.toFixed(2)}</p>
          <p className="text-[11px] text-slate-400 mt-1">{paperPositions.length} active holdings</p>
        </div>

        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
          <span className="text-xs font-bold text-slate-400 block mb-1">TOTAL PORTFOLIO NET WORTH</span>
          <p className="text-2xl font-black text-cyan-400">${netWorth.toFixed(2)}</p>
          <p className="text-[11px] text-slate-400 mt-1">Starting: ${paperAccount.startingCash.toFixed(2)}</p>
        </div>

        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-4">
          <span className="text-xs font-bold text-slate-400 block mb-1">REALIZED PROFIT / LOSS</span>
          <p className={`text-2xl font-black ${paperAccount.realizedPnl >= 0 ? 'text-emerald-400' : 'text-ruby-trading'}`}>
            {paperAccount.realizedPnl >= 0 ? '+' : ''}${paperAccount.realizedPnl.toFixed(2)}
          </p>
          <p className="text-[11px] text-slate-400 mt-1">Closed trades</p>
        </div>
      </div>

      {/* Manual Order Placement Card */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-6">
        <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
          <ShoppingCart className="w-4 h-4 text-cyan-400" />
          <span>SIMULATED ORDER EXECUTION ({selectedDataset.symbol})</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
          {/* Side Toggle */}
          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Order Side</label>
            <div className="flex rounded-lg overflow-hidden border border-[#23386C]">
              <button
                type="button"
                onClick={() => setSide('BUY')}
                className={`flex-1 py-2 text-xs font-bold transition-all ${
                  side === 'BUY' ? 'bg-emerald-500 text-slate-950' : 'bg-[#0B132B] text-slate-400'
                }`}
              >
                BUY (LONG)
              </button>
              <button
                type="button"
                onClick={() => setSide('SELL')}
                className={`flex-1 py-2 text-xs font-bold transition-all ${
                  side === 'SELL' ? 'bg-rose-500 text-white' : 'bg-[#0B132B] text-slate-400'
                }`}
              >
                SELL (EXIT)
              </button>
            </div>
          </div>

          {/* Market Price */}
          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Simulated Market Price</label>
            <input
              type="text"
              readOnly
              value={`$${currentPrice.toFixed(2)}`}
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-slate-300 font-mono"
            />
          </div>

          {/* Quantity */}
          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Shares Quantity</label>
            <input
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400 font-mono"
            />
          </div>

          {/* Submit */}
          <div>
            <button
              onClick={handleOrder}
              className={`w-full py-2.5 rounded-lg text-xs font-bold flex items-center justify-center space-x-2 transition-all ${
                side === 'BUY'
                  ? 'bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow-md shadow-emerald-500/20'
                  : 'bg-rose-500 hover:bg-rose-400 text-white shadow-md shadow-rose-500/20'
              }`}
            >
              <span>Submit {side} Order (${(quantity * currentPrice).toFixed(2)})</span>
            </button>
          </div>
        </div>

        {/* Feedback Message */}
        {feedback && (
          <div
            className={`mt-4 p-3 rounded-lg border text-xs flex items-center space-x-2 ${
              feedback.success
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
            }`}
          >
            {feedback.success ? (
              <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0" />
            ) : (
              <AlertTriangle className="w-4 h-4 text-rose-400 shrink-0" />
            )}
            <span>{feedback.message}</span>
          </div>
        )}
      </div>

      {/* Open Positions Table */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
        <h3 className="text-sm font-bold text-white mb-3">
          Open Positions ({paperPositions.length})
        </h3>
        {paperPositions.length === 0 ? (
          <p className="text-xs text-slate-400">No active positions held. 100% in virtual cash.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="text-[11px] text-slate-400 uppercase bg-[#0B132B] border-b border-[#1B2C56]">
                <tr>
                  <th className="py-2.5 px-3">Symbol</th>
                  <th className="py-2.5 px-3">Quantity</th>
                  <th className="py-2.5 px-3">Avg Cost</th>
                  <th className="py-2.5 px-3">Current Price</th>
                  <th className="py-2.5 px-3">Market Value</th>
                  <th className="py-2.5 px-3">Unrealized P&L</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#1B2C56] text-slate-300 font-mono">
                {paperPositions.map((pos) => (
                  <tr key={pos.symbol} className="hover:bg-[#142244]/50">
                    <td className="py-2 px-3 text-cyan-400 font-bold">{pos.symbol}</td>
                    <td className="py-2 px-3">{pos.quantity}</td>
                    <td className="py-2 px-3">${pos.averagePrice.toFixed(2)}</td>
                    <td className="py-2 px-3 text-white">${pos.currentPrice.toFixed(2)}</td>
                    <td className="py-2 px-3 text-white">${pos.marketValue.toFixed(2)}</td>
                    <td className={`py-2 px-3 font-bold ${pos.unrealizedPnl >= 0 ? 'text-emerald-400' : 'text-ruby-trading'}`}>
                      {pos.unrealizedPnl >= 0 ? '+' : ''}${pos.unrealizedPnl.toFixed(2)} ({pos.unrealizedPnlPct >= 0 ? '+' : ''}{pos.unrealizedPnlPct.toFixed(2)}%)
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Recent Paper Orders */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
        <h3 className="text-sm font-bold text-white mb-3">
          Simulated Orders History ({paperOrders.length})
        </h3>
        <div className="overflow-x-auto max-h-60">
          <table className="w-full text-xs text-left">
            <thead className="text-[11px] text-slate-400 uppercase bg-[#0B132B] sticky top-0 border-b border-[#1B2C56]">
              <tr>
                <th className="py-2.5 px-3">Timestamp</th>
                <th className="py-2.5 px-3">Symbol</th>
                <th className="py-2.5 px-3">Side</th>
                <th className="py-2.5 px-3">Qty</th>
                <th className="py-2.5 px-3">Price</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">Note</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1B2C56] text-slate-300 font-mono">
              {paperOrders.map((ord) => (
                <tr key={ord.id} className="hover:bg-[#142244]/50">
                  <td className="py-2 px-3 text-slate-400 text-[10px]">
                    {new Date(ord.timestamp).toLocaleTimeString()}
                  </td>
                  <td className="py-2 px-3 text-white font-bold">{ord.symbol}</td>
                  <td className={`py-2 px-3 font-bold ${ord.side === 'BUY' ? 'text-emerald-400' : 'text-ruby-trading'}`}>
                    {ord.side}
                  </td>
                  <td className="py-2 px-3">{ord.quantity}</td>
                  <td className="py-2 px-3">${ord.price.toFixed(2)}</td>
                  <td className="py-2 px-3">
                    <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                      {ord.status}
                    </span>
                  </td>
                  <td className="py-2 px-3 text-slate-400 font-sans text-[11px]">{ord.note}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
