import React, { useState } from 'react';
import {
  Menu,
  X,
  LayoutDashboard,
  Database,
  GitCompare,
  PlayCircle,
  Wallet,
  ShieldAlert,
  FileSpreadsheet,
  Settings,
  ChevronDown
} from 'lucide-react';
import { useTrading } from '../context/TradingContext';

export const AndroidTopBar: React.FC = () => {
  const {
    activeTab,
    setActiveTab,
    selectedDataset,
    datasets,
    selectDataset,
    latestSignal
  } = useTrading();

  const [drawerOpen, setDrawerOpen] = useState(false);
  const [selectorOpen, setSelectorOpen] = useState(false);

  const screenTitles: Record<string, string> = {
    dashboard: 'Dashboard',
    'market-data': 'Market Data',
    strategies: 'Strategies',
    backtesting: 'Backtesting',
    'paper-trading': 'Paper Trading',
    'risk-management': 'Risk Controls',
    reports: 'History & Reports',
    settings: 'Settings'
  };

  const navItems = [
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
    <>
      {/* Android Top App Bar */}
      <div className="h-14 bg-[#070C18] border-b border-[#162238] px-3 flex items-center justify-between z-20 shrink-0">
        <div className="flex items-center space-x-2.5">
          <button
            onClick={() => setDrawerOpen(true)}
            className="p-1.5 rounded-full hover:bg-white/10 text-slate-300 transition-colors"
            title="Open Drawer Menu"
          >
            <Menu className="w-5 h-5 text-cyan-400" />
          </button>
          <div>
            <h1 className="text-sm font-extrabold text-white leading-tight">
              {screenTitles[activeTab] || 'AlgoTrader'}
            </h1>
            <p className="text-[10px] text-cyan-400 font-semibold font-mono">
              {selectedDataset.symbol} • {latestSignal?.signal || 'HOLD'}
            </p>
          </div>
        </div>

        {/* Quick Asset Dropdown */}
        <div className="relative">
          <button
            onClick={() => setSelectorOpen(!selectorOpen)}
            className="flex items-center space-x-1 bg-[#101B38] border border-[#23386C] px-2.5 py-1 rounded-lg text-xs font-bold text-white hover:border-cyan-400 transition-colors"
          >
            <span>{selectedDataset.symbol}</span>
            <ChevronDown className="w-3.5 h-3.5 text-cyan-400" />
          </button>

          {selectorOpen && (
            <div className="absolute right-0 mt-1 w-44 bg-[#0B132B] border border-[#23386C] rounded-xl shadow-2xl py-1 z-50">
              {datasets.map((ds) => (
                <button
                  key={ds.id}
                  onClick={() => {
                    selectDataset(ds.id);
                    setSelectorOpen(false);
                  }}
                  className={`w-full text-left px-3 py-1.5 text-xs font-semibold hover:bg-[#142244] flex items-center justify-between ${
                    ds.id === selectedDataset.id ? 'text-cyan-400 bg-[#142244]/60' : 'text-slate-300'
                  }`}
                >
                  <span>{ds.symbol}</span>
                  <span className="text-[9px] text-slate-500 uppercase">{ds.type}</span>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Android Modal Navigation Drawer */}
      {drawerOpen && (
        <div className="fixed inset-0 z-50 flex">
          {/* Backdrop */}
          <div
            className="fixed inset-0 bg-black/60 backdrop-blur-xs"
            onClick={() => setDrawerOpen(false)}
          ></div>

          {/* Drawer Sheet */}
          <div className="relative w-72 max-w-[80%] h-full bg-[#070C18] border-r border-[#1B2C56] p-4 flex flex-col z-50 shadow-2xl animate-in slide-in-from-left duration-200">
            <div className="flex items-center justify-between pb-4 border-b border-[#1B2C56]">
              <div>
                <h2 className="text-base font-black text-white">ALGO TRADER</h2>
                <p className="text-[10px] text-cyan-400 uppercase font-semibold">
                  Android Edition • Kotlin / Compose
                </p>
              </div>
              <button
                onClick={() => setDrawerOpen(false)}
                className="p-1 rounded-full text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="py-3 space-y-1 flex-1 overflow-y-auto">
              {navItems.map((item) => {
                const isActive = activeTab === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => {
                      setActiveTab(item.id);
                      setDrawerOpen(false);
                    }}
                    className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition-all ${
                      isActive
                        ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
                        : 'text-slate-300 hover:bg-[#101B38]'
                    }`}
                  >
                    <div className="flex items-center space-x-3">
                      <span className={isActive ? 'text-cyan-400' : 'text-slate-400'}>
                        {item.icon}
                      </span>
                      <span>{item.label}</span>
                    </div>
                    {item.badge && (
                      <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-400">
                        {item.badge}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>

            <div className="pt-3 border-t border-[#1B2C56] text-[10px] text-slate-500">
              <p>College Capstone Project • Android M3</p>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export const AndroidBottomBar: React.FC = () => {
  const { activeTab, setActiveTab } = useTrading();

  const primaryTabs = [
    { id: 'dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
    { id: 'market-data', label: 'Market', icon: <Database className="w-4 h-4" /> },
    { id: 'strategies', label: 'Strategies', icon: <GitCompare className="w-4 h-4" /> },
    { id: 'backtesting', label: 'Backtest', icon: <PlayCircle className="w-4 h-4" /> },
    { id: 'paper-trading', label: 'Paper Trade', icon: <Wallet className="w-4 h-4" /> },
  ];

  return (
    <nav className="h-14 bg-[#070C18] border-t border-[#162238] flex items-center justify-around px-1 z-20 shrink-0">
      {primaryTabs.map((tab) => {
        const isActive = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex flex-col items-center justify-center py-1 px-2 rounded-xl transition-all ${
              isActive ? 'text-cyan-400' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            {/* Android M3 active pill indicator */}
            <div
              className={`px-3 py-1 rounded-full transition-all flex items-center justify-center ${
                isActive ? 'bg-[#142244] text-cyan-400' : 'bg-transparent text-slate-400'
              }`}
            >
              {tab.icon}
            </div>
            <span className={`text-[10px] mt-0.5 font-medium ${isActive ? 'font-bold text-cyan-400' : ''}`}>
              {tab.label}
            </span>
          </button>
        );
      })}
    </nav>
  );
};
