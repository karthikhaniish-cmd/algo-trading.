import React, { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import { RotateCcw, Info, Shield, CheckCircle, Smartphone, Terminal, Download } from 'lucide-react';

export const SettingsView: React.FC = () => {
  const { resetAllDemoData } = useTrading();
  const [showConfirm, setShowConfirm] = useState(false);
  const [resetSuccess, setResetSuccess] = useState(false);

  const handleReset = () => {
    resetAllDemoData();
    setShowConfirm(false);
    setResetSuccess(true);
    setTimeout(() => setResetSuccess(false), 3000);
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-black text-white">Application Settings & Android Build Info</h2>
        <p className="text-xs text-slate-400">
          Configure default simulation settings, review Android native architecture, or build APK.
        </p>
      </div>

      {/* Android Native APK & Studio Info Card */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5 space-y-3">
        <div className="flex items-center space-x-2 text-cyan-400">
          <Smartphone className="w-5 h-5" />
          <h3 className="text-sm font-bold uppercase tracking-wide">
            Native Android Project (Jetpack Compose & Kotlin)
          </h3>
        </div>
        <p className="text-xs text-slate-300 leading-relaxed">
          This repository includes both a responsive Android-first client and a complete <strong>native Android Studio project</strong> written in Kotlin with Jetpack Compose Material 3 and Room SQLite database located in <code className="text-cyan-400 bg-slate-900 px-1.5 py-0.5 rounded font-mono">/app</code>.
        </p>

        <div className="bg-[#070C18] border border-[#162238] rounded-lg p-3 text-xs font-mono space-y-2">
          <div className="flex items-center space-x-2 text-slate-400 text-[11px]">
            <Terminal className="w-3.5 h-3.5 text-cyan-400" />
            <span>How to compile the APK in Android Studio:</span>
          </div>
          <div className="text-slate-300 select-all bg-[#0B132B] p-2 rounded border border-[#1B2C56] text-[11px]">
            <p className="text-slate-400"># 1. Open project root in Android Studio Ladybug / Meerkat</p>
            <p className="text-cyan-300"># 2. Build Debug APK:</p>
            <p className="text-white font-bold">gradle assembleDebug</p>
            <p className="text-slate-400 mt-1"># Output APK location:</p>
            <p className="text-emerald-400">app/build/outputs/apk/debug/app-debug.apk</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs text-slate-400 pt-1">
          <div className="bg-[#0B132B] p-2.5 rounded-lg border border-[#1B2C56]">
            <span className="font-bold text-white block">Kotlin Compose UI</span>
            <span>MainActivity.kt, Material 3, Dark Navy theme</span>
          </div>
          <div className="bg-[#0B132B] p-2.5 rounded-lg border border-[#1B2C56]">
            <span className="font-bold text-white block">Room SQLite Database</span>
            <span>TradingDao, TradingDatabase, Entity tables</span>
          </div>
        </div>
      </div>

      {/* Database Reset Card */}
      <div className="bg-[#101B38] border border-[#1B2C56] rounded-xl p-5">
        <h3 className="text-sm font-bold text-rose-400 mb-2 flex items-center space-x-2">
          <RotateCcw className="w-4 h-4" />
          <span>RESET DEMO DATABASE & BALANCES</span>
        </h3>
        <p className="text-xs text-slate-400 mb-4 leading-relaxed">
          Restores initial synthetic datasets (AAPL, TSLA, SPY, BTC-USD), clears paper trading orders, and resets virtual cash balance to $10,000.00.
        </p>

        {showConfirm ? (
          <div className="bg-rose-500/10 border border-rose-500/30 p-3 rounded-lg flex flex-col md:flex-row items-center justify-between gap-3">
            <span className="text-xs font-bold text-rose-300">
              Confirm database reset? All current paper trades will be cleared.
            </span>
            <div className="flex space-x-2">
              <button
                onClick={handleReset}
                className="bg-rose-600 hover:bg-rose-500 text-white font-bold px-3 py-1.5 rounded text-xs transition-all"
              >
                Yes, Reset
              </button>
              <button
                onClick={() => setShowConfirm(false)}
                className="bg-[#142244] hover:bg-[#1B2C56] text-slate-300 px-3 py-1.5 rounded text-xs font-bold transition-all"
              >
                Cancel
              </button>
            </div>
          </div>
        ) : (
          <button
            onClick={() => setShowConfirm(true)}
            className="bg-rose-600/20 hover:bg-rose-600/30 text-rose-400 border border-rose-500/40 font-bold px-4 py-2 rounded-lg text-xs flex items-center space-x-2 transition-all"
          >
            <RotateCcw className="w-4 h-4" />
            <span>Reset All Data to Factory Defaults</span>
          </button>
        )}

        {resetSuccess && (
          <div className="mt-3 p-3 bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 rounded-lg text-xs flex items-center space-x-2">
            <CheckCircle className="w-4 h-4 text-emerald-400" />
            <span>Demo state restored successfully!</span>
          </div>
        )}
      </div>

      {/* Safety Notice */}
      <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-xl p-4 flex items-center space-x-3 text-xs text-emerald-300">
        <Shield className="w-5 h-5 text-emerald-400 shrink-0" />
        <p>
          Verified Safe: This application strictly isolates all operations in local simulation memory and SQLite database. Real money execution is permanently disabled.
        </p>
      </div>
    </div>
  );
};
