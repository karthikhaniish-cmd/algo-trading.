import React, { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import { Database, Upload, Sparkles, CheckCircle2, AlertTriangle, Trash2 } from 'lucide-react';
import { CsvValidationResult } from '../utils/csvParser';

export const MarketDataView: React.FC = () => {
  const {
    datasets,
    selectedDataset,
    selectDataset,
    deleteDataset,
    importCsvDataset,
    createSyntheticDataset
  } = useTrading();

  const [activeTab, setActiveTab] = useState<'datasets' | 'csv' | 'generator'>('datasets');

  // CSV Form
  const [csvSymbol, setCsvSymbol] = useState('MSFT');
  const [csvName, setCsvName] = useState('Microsoft Corp (CSV)');
  const [csvDesc, setCsvDesc] = useState('Imported historical daily OHLCV bars');
  const [csvText, setCsvText] = useState(`Date,Open,High,Low,Close,Volume
2026-01-02,410.50,414.20,409.80,413.90,1850000
2026-01-05,414.00,418.50,413.20,417.80,2100000
2026-01-06,417.50,420.00,416.60,418.20,1950000
2026-01-07,418.00,422.20,417.80,421.90,2300000
2026-01-08,422.10,424.00,420.50,421.10,2050000
2026-01-09,421.00,426.50,420.80,425.90,2600000
2026-01-12,426.00,429.20,425.10,428.80,2850000
2026-01-13,429.00,432.30,427.90,430.60,2400000
2026-01-14,431.00,433.00,429.50,431.40,2150000
2026-01-15,431.20,435.40,430.80,434.90,2700000`);
  const [validationResult, setValidationResult] = useState<CsvValidationResult | null>(null);

  // Synthetic Generator Form
  const [synthSymbol, setSynthSymbol] = useState('NVDA');
  const [synthName, setSynthName] = useState('NVIDIA Corp (Synthetic)');
  const [synthStartPrice, setSynthStartPrice] = useState(120);
  const [synthVol, setSynthVol] = useState(0.35);
  const [synthDrift, setSynthDrift] = useState(0.20);
  const [synthBars, setSynthBars] = useState(120);

  const handleImportCsv = () => {
    const res = importCsvDataset(csvSymbol, csvName, csvDesc, csvText);
    setValidationResult(res);
  };

  const handleCreateSynthetic = () => {
    createSyntheticDataset(
      synthSymbol,
      synthName,
      `Geometric Brownian Motion (Vol=${synthVol}, Drift=${synthDrift})`,
      synthStartPrice,
      synthVol,
      synthDrift,
      synthBars
    );
    setActiveTab('datasets');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-black text-white">Market Data & Datasets</h2>
        <p className="text-xs text-slate-400">
          Manage historical stock price datasets, upload CSV files with strict OHLC validation, or generate synthetic Geometric Brownian Motion data.
        </p>
      </div>

      {/* Tabs */}
      <div className="flex space-x-2 border-b border-[#1B2C56] pb-2">
        <button
          onClick={() => setActiveTab('datasets')}
          className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-bold transition-all ${
            activeTab === 'datasets'
              ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/40'
              : 'text-slate-400 hover:text-white bg-[#101B38]'
          }`}
        >
          <Database className="w-4 h-4" />
          <span>Active Datasets ({datasets.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('csv')}
          className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-bold transition-all ${
            activeTab === 'csv'
              ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/40'
              : 'text-slate-400 hover:text-white bg-[#101B38]'
          }`}
        >
          <Upload className="w-4 h-4" />
          <span>Upload / Paste CSV</span>
        </button>

        <button
          onClick={() => setActiveTab('generator')}
          className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-bold transition-all ${
            activeTab === 'generator'
              ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/40'
              : 'text-slate-400 hover:text-white bg-[#101B38]'
          }`}
        >
          <Sparkles className="w-4 h-4" />
          <span>Synthetic Generator</span>
        </button>
      </div>

      {/* Tab 1: Datasets */}
      {activeTab === 'datasets' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {datasets.map((ds) => {
              const isSelected = ds.id === selectedDataset.id;
              return (
                <div
                  key={ds.id}
                  onClick={() => selectDataset(ds.id)}
                  className={`p-4 rounded-xl border cursor-pointer transition-all ${
                    isSelected
                      ? 'bg-[#142244] border-cyan-400 shadow-md shadow-cyan-500/10'
                      : 'bg-[#101B38] border-[#1B2C56] hover:border-[#2F4885]'
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center space-x-2">
                      <span className="text-lg font-black text-white">{ds.symbol}</span>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                        {ds.type}
                      </span>
                    </div>
                    {datasets.length > 1 && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          deleteDataset(ds.id);
                        }}
                        className="text-slate-500 hover:text-rose-400 transition-colors p-1"
                        title="Delete dataset"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                  <p className="text-xs font-semibold text-slate-300">{ds.name}</p>
                  <p className="text-[11px] text-slate-400 mt-1 line-clamp-1">{ds.description}</p>
                  <div className="mt-3 pt-2 border-t border-[#1B2C56] flex items-center justify-between text-[11px] text-slate-400 font-mono">
                    <span>{ds.bars.length} Bars</span>
                    <span>{ds.bars[0]?.date} → {ds.bars[ds.bars.length - 1]?.date}</span>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Dataset Bar Inspection Table */}
          <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
            <h3 className="text-sm font-bold text-white mb-3">
              Dataset Inspection: {selectedDataset.symbol} ({selectedDataset.bars.length} bars)
            </h3>
            <div className="overflow-x-auto max-h-72">
              <table className="w-full text-xs text-left">
                <thead className="text-[11px] text-slate-400 uppercase bg-[#0B132B] sticky top-0 border-b border-[#1B2C56]">
                  <tr>
                    <th className="py-2.5 px-3">Date</th>
                    <th className="py-2.5 px-3">Open</th>
                    <th className="py-2.5 px-3">High</th>
                    <th className="py-2.5 px-3">Low</th>
                    <th className="py-2.5 px-3">Close</th>
                    <th className="py-2.5 px-3">Volume</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#1B2C56] text-slate-300 font-mono">
                  {selectedDataset.bars.slice(-15).reverse().map((bar, idx) => (
                    <tr key={idx} className="hover:bg-[#142244]/50">
                      <td className="py-2 px-3 text-white font-semibold">{bar.date}</td>
                      <td className="py-2 px-3">${bar.open.toFixed(2)}</td>
                      <td className="py-2 px-3 text-emerald-400">${bar.high.toFixed(2)}</td>
                      <td className="py-2 px-3 text-ruby-trading">${bar.low.toFixed(2)}</td>
                      <td className="py-2 px-3 font-bold text-white">${bar.close.toFixed(2)}</td>
                      <td className="py-2 px-3">{bar.volume.toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: CSV Import */}
      {activeTab === 'csv' && (
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Symbol</label>
              <input
                type="text"
                value={csvSymbol}
                onChange={(e) => setCsvSymbol(e.target.value)}
                className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Dataset Name</label>
              <input
                type="text"
                value={csvName}
                onChange={(e) => setCsvName(e.target.value)}
                className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
          </div>

          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Description</label>
            <input
              type="text"
              value={csvDesc}
              onChange={(e) => setCsvDesc(e.target.value)}
              className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
            />
          </div>

          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">
              CSV Content (Header: Date,Open,High,Low,Close,Volume)
            </label>
            <textarea
              rows={8}
              value={csvText}
              onChange={(e) => setCsvText(e.target.value)}
              className="w-full font-mono text-xs bg-[#0B132B] border border-[#23386C] rounded-lg p-3 text-slate-200 focus:outline-none focus:border-cyan-400"
            />
          </div>

          {/* Validation Feedback */}
          {validationResult && (
            <div
              className={`p-3 rounded-lg border text-xs ${
                validationResult.isValid
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                  : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
              }`}
            >
              <div className="flex items-center space-x-2 font-bold mb-1">
                {validationResult.isValid ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                ) : (
                  <AlertTriangle className="w-4 h-4 text-rose-400" />
                )}
                <span>
                  {validationResult.isValid
                    ? `Validation Success! Loaded ${validationResult.validBars.length} bars.`
                    : 'Validation Errors Detected:'}
                </span>
              </div>
              {validationResult.errors.map((err, i) => (
                <p key={i} className="text-[11px] ml-6">• {err}</p>
              ))}
            </div>
          )}

          <button
            onClick={handleImportCsv}
            className="w-full bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold py-2.5 rounded-lg text-sm flex items-center justify-center space-x-2 transition-all shadow-md shadow-cyan-500/20"
          >
            <Upload className="w-4 h-4" />
            <span>Validate & Import CSV Dataset</span>
          </button>
        </div>
      )}

      {/* Tab 3: Synthetic Generator */}
      {activeTab === 'generator' && (
        <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-6 space-y-4">
          <p className="text-xs text-slate-400">
            Geometric Brownian Motion stochastic differential equation:
            <span className="font-mono text-cyan-400 block mt-1">
              dS = μ S dt + σ S dW
            </span>
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Symbol</label>
              <input
                type="text"
                value={synthSymbol}
                onChange={(e) => setSynthSymbol(e.target.value)}
                className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Asset Name</label>
              <input
                type="text"
                value={synthName}
                onChange={(e) => setSynthName(e.target.value)}
                className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Start Price ($)</label>
              <input
                type="number"
                value={synthStartPrice}
                onChange={(e) => setSynthStartPrice(Number(e.target.value))}
                className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Volatility (σ)</label>
              <input
                type="number"
                step="0.05"
                value={synthVol}
                onChange={(e) => setSynthVol(Number(e.target.value))}
                className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Annual Drift (μ)</label>
              <input
                type="number"
                step="0.05"
                value={synthDrift}
                onChange={(e) => setSynthDrift(Number(e.target.value))}
                className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Bars Count</label>
              <input
                type="number"
                value={synthBars}
                onChange={(e) => setSynthBars(Number(e.target.value))}
                className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
          </div>

          <button
            onClick={handleCreateSynthetic}
            className="w-full bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold py-2.5 rounded-lg text-sm flex items-center justify-center space-x-2 transition-all shadow-md shadow-cyan-500/20"
          >
            <Sparkles className="w-4 h-4" />
            <span>Simulate & Generate Dataset</span>
          </button>
        </div>
      )}
    </div>
  );
};
