import React, { useState } from 'react';
import { TradingProvider, useTrading } from './context/TradingContext';
import { Sidebar } from './components/Sidebar';
import { Navbar } from './components/Navbar';
import { TickerBar } from './components/TickerBar';
import { DashboardView } from './views/DashboardView';
import { MarketDataView } from './views/MarketDataView';
import { StrategiesView } from './views/StrategiesView';
import { BacktestingView } from './views/BacktestingView';
import { PaperTradingView } from './views/PaperTradingView';
import { RiskManagementView } from './views/RiskManagementView';
import { ReportsView } from './views/ReportsView';
import { SettingsView } from './views/SettingsView';
import { Menu, X } from 'lucide-react';

const MainContent: React.FC = () => {
  const { activeTab } = useTrading();

  return (
    <main className="flex-1 overflow-y-auto p-4 md:p-8 bg-[#0B132B]">
      <div className="max-w-[1600px] mx-auto space-y-6">
        {activeTab === 'dashboard' && <DashboardView />}
        {activeTab === 'market-data' && <MarketDataView />}
        {activeTab === 'strategies' && <StrategiesView />}
        {activeTab === 'backtesting' && <BacktestingView />}
        {activeTab === 'paper-trading' && <PaperTradingView />}
        {activeTab === 'risk-management' && <RiskManagementView />}
        {activeTab === 'reports' && <ReportsView />}
        {activeTab === 'settings' && <SettingsView />}
      </div>
    </main>
  );
};

const WebAppLayout: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <div className="flex h-screen w-screen bg-[#070C18] text-slate-100 overflow-hidden font-sans">
      {/* Desktop Sidebar */}
      <div className="hidden lg:flex shrink-0">
        <Sidebar />
      </div>

      {/* Mobile Drawer Overlay */}
      {mobileMenuOpen && (
        <div className="fixed inset-0 z-50 lg:hidden flex">
          <div
            className="fixed inset-0 bg-black/60 backdrop-blur-xs"
            onClick={() => setMobileMenuOpen(false)}
          ></div>
          <div className="relative w-64 max-w-[80%] h-full bg-[#070C18] z-50 shadow-2xl flex flex-col">
            <div className="p-4 border-b border-[#1B2C56] flex items-center justify-between">
              <span className="font-extrabold text-white text-sm">MENU</span>
              <button
                onClick={() => setMobileMenuOpen(false)}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="flex-1 overflow-y-auto">
              <Sidebar />
            </div>
          </div>
        </div>
      )}

      {/* Main View Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Ticker Bar at Top */}
        <TickerBar />

        {/* Top Navbar */}
        <div className="flex items-center">
          {/* Mobile Menu Hamburger */}
          <button
            onClick={() => setMobileMenuOpen(true)}
            className="lg:hidden p-3 bg-[#0B132B] text-slate-300 hover:text-white border-b border-[#1B2C56] border-r"
            title="Open Menu"
          >
            <Menu className="w-5 h-5" />
          </button>
          <div className="flex-1 min-w-0">
            <Navbar />
          </div>
        </div>

        {/* Content Body */}
        <MainContent />
      </div>
    </div>
  );
};

export function App() {
  return (
    <TradingProvider>
      <WebAppLayout />
    </TradingProvider>
  );
}

export default App;
