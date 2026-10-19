import React, { useState, useEffect } from 'react';
import { Smartphone, Monitor, Wifi, BatteryMedium, Signal, Download } from 'lucide-react';

interface AndroidFrameProps {
  children: React.ReactNode;
}

export const AndroidFrame: React.FC<AndroidFrameProps> = ({ children }) => {
  const [deviceMode, setDeviceMode] = useState<'phone' | 'fullscreen'>('phone');
  const [currentTime, setCurrentTime] = useState('09:41');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setCurrentTime(
        now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })
      );
    };
    updateTime();
    const interval = setInterval(updateTime, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen bg-[#050811] flex flex-col items-center justify-start overflow-hidden">
      {/* Top Device Bar Switcher */}
      <div className="w-full bg-[#070C18] border-b border-[#1B2C56] px-4 py-2 flex items-center justify-between text-xs z-50 shrink-0">
        <div className="flex items-center space-x-2">
          <div className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse"></div>
          <span className="font-extrabold text-white text-xs tracking-wider">
            ALGO TRADER • ANDROID APP
          </span>
          <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            Material 3 OS
          </span>
        </div>

        {/* View Mode Controls */}
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setDeviceMode('phone')}
            className={`flex items-center space-x-1.5 px-3 py-1 rounded-lg text-xs font-bold transition-all ${
              deviceMode === 'phone'
                ? 'bg-cyan-500 text-slate-950 font-black shadow-md shadow-cyan-500/20'
                : 'text-slate-400 hover:text-white bg-[#101B38]'
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            <span>Android Phone</span>
          </button>

          <button
            onClick={() => setDeviceMode('fullscreen')}
            className={`flex items-center space-x-1.5 px-3 py-1 rounded-lg text-xs font-bold transition-all ${
              deviceMode === 'fullscreen'
                ? 'bg-cyan-500 text-slate-950 font-black shadow-md shadow-cyan-500/20'
                : 'text-slate-400 hover:text-white bg-[#101B38]'
            }`}
          >
            <Monitor className="w-3.5 h-3.5" />
            <span>Responsive / Tablet</span>
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 w-full flex items-center justify-center p-0 md:p-4 overflow-hidden">
        {deviceMode === 'phone' ? (
          /* Phone Frame (Google Pixel style) */
          <div className="relative w-full max-w-[420px] h-[92vh] max-h-[880px] bg-[#070C18] border-[8px] border-[#162238] rounded-[44px] shadow-2xl shadow-cyan-500/10 flex flex-col overflow-hidden ring-1 ring-white/10">
            {/* Top Speaker / Notch & Status Bar */}
            <div className="h-9 bg-[#070C18] px-6 flex items-center justify-between text-[11px] text-slate-300 select-none z-30 shrink-0 border-b border-[#142038]">
              <span className="font-bold font-mono">{currentTime}</span>

              {/* Camera Punch Hole */}
              <div className="w-3.5 h-3.5 rounded-full bg-black border border-slate-700/60 shadow-inner"></div>

              {/* Status Icons */}
              <div className="flex items-center space-x-1.5 text-slate-300">
                <Signal className="w-3.5 h-3.5" />
                <Wifi className="w-3.5 h-3.5" />
                <BatteryMedium className="w-4 h-4 text-emerald-400" />
              </div>
            </div>

            {/* Android Screen Inner */}
            <div className="flex-1 flex flex-col min-h-0 bg-[#0B132B] overflow-hidden">
              {children}
            </div>

            {/* Android Gesture Navigation Bar */}
            <div className="h-5 bg-[#070C18] flex items-center justify-center z-30 shrink-0 border-t border-[#142038]">
              <div className="w-28 h-1 rounded-full bg-slate-500/50"></div>
            </div>
          </div>
        ) : (
          /* Fullscreen Responsive Mode */
          <div className="w-full h-full flex flex-col overflow-hidden">
            {children}
          </div>
        )}
      </div>
    </div>
  );
};
