import React, { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import { ShieldAlert, ShieldCheck, AlertCircle, Percent } from 'lucide-react';

export const RiskManagementView: React.FC = () => {
  const {
    riskConfig,
    updateRiskConfig,
    paperAccount,
    paperPositions,
    selectedDataset,
    bars
  } = useTrading();

  const [testQty, setTestQty] = useState(25);
  const currentPrice = bars[bars.length - 1]?.close || 150;
  const totalPortfolioVal = paperAccount.cash + paperPositions.reduce((sum, p) => sum + p.marketValue, 0);

  // Pre-trade evaluation
  const proposedCost = testQty * currentPrice;
  const existingPos = paperPositions.find(p => p.symbol.toUpperCase() === selectedDataset.symbol.toUpperCase());
  const existingVal = existingPos ? existingPos.marketValue : 0;
  const maxAllowedForSymbol = totalPortfolioVal * (riskConfig.maxPositionSizePct / 100);
  const totalInvestedAfter = paperPositions.reduce((sum, p) => sum + p.marketValue, 0) + proposedCost;
  const maxAllowedExposure = totalPortfolioVal * (riskConfig.maxPortfolioExposurePct / 100);

  const exceedsPositionCap = existingVal + proposedCost > maxAllowedForSymbol;
  const exceedsExposureCap = totalInvestedAfter > maxAllowedExposure;
  const exceedsCash = proposedCost > paperAccount.cash;

  const isOrderAllowed = !exceedsPositionCap && !exceedsExposureCap && !exceedsCash;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-black text-white">Risk Management & Capital Preservation</h2>
        <p className="text-xs text-slate-400">
          Enforce portfolio allocation limits, position concentration caps, and stop-loss rules to minimize drawdowns.
        </p>
      </div>

      {/* Risk Controls Form */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-6">
        <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
          <ShieldAlert className="w-4 h-4 text-cyan-400" />
          <span>PORTFOLIO CONSTRAINTS</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Max Position Size */}
          <div className="space-y-2">
            <div className="flex justify-between text-xs font-bold text-slate-300">
              <span>Max Position Concentration</span>
              <span className="text-cyan-400 font-mono">{riskConfig.maxPositionSizePct}% of Portfolio</span>
            </div>
            <input
              type="range"
              min={5}
              max={100}
              value={riskConfig.maxPositionSizePct}
              onChange={(e) => updateRiskConfig({ maxPositionSizePct: Number(e.target.value) })}
              className="w-full accent-cyan-400 cursor-pointer"
            />
            <p className="text-[11px] text-slate-500">
              Limits exposure to any individual security to prevent catastrophic black-swan events.
            </p>
          </div>

          {/* Max Portfolio Exposure */}
          <div className="space-y-2">
            <div className="flex justify-between text-xs font-bold text-slate-300">
              <span>Max Total Portfolio Exposure</span>
              <span className="text-amber-400 font-mono">{riskConfig.maxPortfolioExposurePct}%</span>
            </div>
            <input
              type="range"
              min={20}
              max={100}
              value={riskConfig.maxPortfolioExposurePct}
              onChange={(e) => updateRiskConfig({ maxPortfolioExposurePct: Number(e.target.value) })}
              className="w-full accent-amber-400 cursor-pointer"
            />
            <p className="text-[11px] text-slate-500">
              Guarantees at least {100 - riskConfig.maxPortfolioExposurePct}% liquid cash buffer at all times.
            </p>
          </div>
        </div>
      </div>

      {/* Pre-Trade Risk Simulator */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-6">
        <h3 className="text-sm font-bold text-white mb-2 flex items-center space-x-2">
          <Percent className="w-4 h-4 text-emerald-400" />
          <span>PRE-TRADE RISK VALIDATOR SIMULATOR</span>
        </h3>
        <p className="text-xs text-slate-400 mb-4">
          Test order sizes on <span className="text-white font-bold">{selectedDataset.symbol}</span> @ ${currentPrice.toFixed(2)} to verify risk limit compliance.
        </p>

        <div className="flex items-center space-x-4 mb-4">
          <div className="w-48">
            <label className="text-xs font-bold text-slate-300 block mb-1">Proposed Shares</label>
            <input
              type="number"
              min={1}
              value={testQty}
              onChange={(e) => setTestQty(Number(e.target.value))}
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400 font-mono"
            />
          </div>
          <div className="pt-5 text-sm text-slate-300 font-mono">
            Total Cost: <span className="font-bold text-white">${proposedCost.toFixed(2)}</span>
          </div>
        </div>

        {/* Validation Result Box */}
        <div
          className={`p-4 rounded-xl border text-xs ${
            isOrderAllowed
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
              : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
          }`}
        >
          <div className="flex items-center space-x-2 font-bold mb-1">
            {isOrderAllowed ? (
              <ShieldCheck className="w-5 h-5 text-emerald-400" />
            ) : (
              <AlertCircle className="w-5 h-5 text-rose-400" />
            )}
            <span className="text-sm">
              {isOrderAllowed ? 'Risk Check Passed: Order Allowed' : 'Risk Check Failed: Order Blocked'}
            </span>
          </div>
          <div className="space-y-1 ml-7 text-[11px]">
            {exceedsCash && <p>• Insufficient cash available (${paperAccount.cash.toFixed(2)} available vs ${proposedCost.toFixed(2)} required).</p>}
            {exceedsPositionCap && (
              <p>
                • Exceeds {riskConfig.maxPositionSizePct}% position concentration cap ($
                {maxAllowedForSymbol.toFixed(2)} maximum allowed in {selectedDataset.symbol}).
              </p>
            )}
            {exceedsExposureCap && (
              <p>
                • Exceeds {riskConfig.maxPortfolioExposurePct}% total portfolio exposure cap ($
                {maxAllowedExposure.toFixed(2)} maximum invested capital).
              </p>
            )}
            {isOrderAllowed && <p>• Order sizing complies with position and cash exposure limits.</p>}
          </div>
        </div>
      </div>
    </div>
  );
};
