import React, { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import { Download, Search, FileSpreadsheet, Check, Copy } from 'lucide-react';

export const ReportsView: React.FC = () => {
  const { paperOrders, paperAccount } = useTrading();
  const [search, setSearch] = useState('');
  const [sideFilter, setSideFilter] = useState<'ALL' | 'BUY' | 'SELL'>('ALL');
  const [copied, setCopied] = useState(false);

  const filtered = paperOrders.filter(o => {
    const matchesSide = sideFilter === 'ALL' || o.side === sideFilter;
    const matchesSearch = o.symbol.toLowerCase().includes(search.toLowerCase()) ||
      o.strategy.toLowerCase().includes(search.toLowerCase());
    return matchesSide && matchesSearch;
  });

  const generateCsv = () => {
    let csv = 'Order_ID,Timestamp,Date_Time,Symbol,Side,Quantity,Price,Total_Value,Strategy,Status,Realized_PnL,Note\n';
    paperOrders.forEach(o => {
      const dt = new Date(o.timestamp).toISOString();
      const tot = o.quantity * o.price;
      csv += `${o.id},${o.timestamp},${dt},${o.symbol},${o.side},${o.quantity},${o.price},${tot.toFixed(2)},"${o.strategy}",${o.status},${o.realizedPnl},"${o.note}"\n`;
    });
    return csv;
  };

  const downloadCsv = () => {
    const csv = generateCsv();
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `trade_history_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const copyCsv = () => {
    navigator.clipboard.writeText(generateCsv());
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-black text-white">Trade History & Audit Reports</h2>
          <p className="text-xs text-slate-400">
            Searchable log of all simulated executions, performance audit trails, and RFC-4180 CSV export.
          </p>
        </div>

        <div className="flex space-x-2">
          <button
            onClick={copyCsv}
            className="bg-[#142244] hover:bg-[#1B2C56] border border-[#23386C] text-slate-200 text-xs font-bold px-3 py-2 rounded-lg flex items-center space-x-1.5 transition-all"
          >
            {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4 text-cyan-400" />}
            <span>{copied ? 'Copied to Clipboard' : 'Copy CSV'}</span>
          </button>

          <button
            onClick={downloadCsv}
            className="bg-cyan-500 hover:bg-cyan-400 text-slate-950 text-xs font-bold px-4 py-2 rounded-lg flex items-center space-x-1.5 transition-all shadow-md shadow-cyan-500/20"
          >
            <Download className="w-4 h-4" />
            <span>Download CSV Report</span>
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col md:flex-row gap-4 items-center justify-between bg-[#101B38] border border-[#1B2C56] p-4 rounded-xl">
        <div className="relative w-full md:w-80">
          <input
            type="text"
            placeholder="Search by symbol or strategy..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-[#0B132B] border border-[#23386C] rounded-lg pl-9 pr-3 py-2 text-xs text-white focus:outline-none focus:border-cyan-400"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
        </div>

        <div className="flex space-x-1.5">
          {(['ALL', 'BUY', 'SELL'] as const).map(s => (
            <button
              key={s}
              onClick={() => setSideFilter(s)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                sideFilter === s
                  ? 'bg-cyan-500 text-slate-950 font-black'
                  : 'bg-[#142244] text-slate-400 hover:text-white'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* Order Table */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
        <div className="overflow-x-auto max-h-96">
          <table className="w-full text-xs text-left">
            <thead className="text-[11px] text-slate-400 uppercase bg-[#0B132B] sticky top-0 border-b border-[#1B2C56]">
              <tr>
                <th className="py-2.5 px-3">Date / Time</th>
                <th className="py-2.5 px-3">Symbol</th>
                <th className="py-2.5 px-3">Side</th>
                <th className="py-2.5 px-3">Shares</th>
                <th className="py-2.5 px-3">Price</th>
                <th className="py-2.5 px-3">Total ($)</th>
                <th className="py-2.5 px-3">Realized P&L</th>
                <th className="py-2.5 px-3">Strategy</th>
                <th className="py-2.5 px-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1B2C56] text-slate-300 font-mono">
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={9} className="py-8 text-center text-slate-500 font-sans">
                    No matching orders found.
                  </td>
                </tr>
              ) : (
                filtered.map((ord) => (
                  <tr key={ord.id} className="hover:bg-[#142244]/50">
                    <td className="py-2 px-3 text-slate-400 text-[10px]">
                      {new Date(ord.timestamp).toLocaleString()}
                    </td>
                    <td className="py-2 px-3 text-cyan-400 font-bold">{ord.symbol}</td>
                    <td className={`py-2 px-3 font-bold ${ord.side === 'BUY' ? 'text-emerald-400' : 'text-ruby-trading'}`}>
                      {ord.side}
                    </td>
                    <td className="py-2 px-3">{ord.quantity}</td>
                    <td className="py-2 px-3">${ord.price.toFixed(2)}</td>
                    <td className="py-2 px-3 text-white">${(ord.quantity * ord.price).toFixed(2)}</td>
                    <td className={`py-2 px-3 font-bold ${ord.realizedPnl >= 0 ? 'text-emerald-400' : 'text-ruby-trading'}`}>
                      {ord.realizedPnl !== 0 ? `${ord.realizedPnl >= 0 ? '+' : ''}$${ord.realizedPnl.toFixed(2)}` : '—'}
                    </td>
                    <td className="py-2 px-3 text-slate-300 font-sans">{ord.strategy}</td>
                    <td className="py-2 px-3">
                      <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        {ord.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
