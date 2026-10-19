import React from 'react';
import {
  LayoutDashboard,
  Database,
  GitCompare,
  PlayCircle,
  Wallet,
  ShieldAlert,
  FileSpreadsheet,
  Settings,
  TrendingUp,
  Activity
} from 'lucide-react';
import { useTrading } from '../context/TradingContext';

interface NavItem {
  id: string;
  label: string;
  icon: React.ReactNode;
  badge?: string;
}

export const Sidebar: React.FC = () => {
  const { activeTab, setActiveTab } = useTrading();

  const navItems: NavItem[] = [
    { id: 'dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
    { id: 'market-data', label: 'Market Data', icon: <Database className="w-5 h-5" /> },
    { id: 'strategies', label: 'Strategies', icon: <GitCompare className="w-5 h-5" /> },
    { id: 'backtesting', label: 'Backtesting', icon: <PlayCircle className="w-5 h-5" /> },
    { id: 'paper-trading', label: 'Paper Trading', icon: <Wallet className="w-5 h-5" />, badge: 'VIRTUAL' },
    { id: 'risk-management', label: 'Risk Controls', icon: <ShieldAlert className="w-5 h-5" /> },
    { id: 'reports', label: 'History & Reports', icon: <FileSpreadsheet className="w-5 h-5" /> },
    { id: 'settings', label: 'Settings', icon: <Settings className="w-5 h-5" /> },
  ];

  return (
    <aside className="w-64 bg-[#070C18] border-r border-[#1B2C56] flex flex-col shrink-0 select-none">
      {/* Brand Header */}
      <div className="p-5 border-b border-[#1B2C56]">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-cyan-400 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
            <TrendingUp className="w-5 h-5 text-slate-950 font-bold" />
          </div>
          <div>
            <h1 className="text-base font-extrabold tracking-wide text-white">ALGO TRADING</h1>
            <p className="text-[10px] text-cyan-400 font-medium tracking-wider uppercase">Quantitative Platform</p>
          </div>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="p-3 space-y-1.5 flex-1 overflow-y-auto">
        {navItems.map((item) => {
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
                isActive
                  ? 'bg-[#142244] text-cyan-400 border border-cyan-500/30 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-[#101B38]'
              }`}
            >
              <div className="flex items-center space-x-3">
                <span className={isActive ? 'text-cyan-400' : 'text-slate-400'}>{item.icon}</span>
                <span>{item.label}</span>
              </div>
              {item.badge && (
                <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-400 border border-amber-500/30">
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      {/* System Status Footer */}
      <div className="p-4 border-t border-[#1B2C56] bg-[#090F1E]">
        <div className="flex items-center space-x-2 text-xs text-slate-400">
          <Activity className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
          <span className="text-[11px] font-semibold text-slate-300">Simulation Engine Active</span>
        </div>
        <p className="text-[10px] text-slate-500 mt-1">College Final-Year Project</p>
      </div>
    </aside>
  );
};
