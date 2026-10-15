import React, { useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { Radio, ShieldCheck, Zap } from 'lucide-react';

export const SplashScreen: React.FC = () => {
  const { currentUser, navigateTo } = useApp();

  useEffect(() => {
    const timer = setTimeout(() => {
      if (currentUser) {
        navigateTo('DASHBOARD');
      } else {
        navigateTo('LANDING');
      }
    }, 2400);
    return () => clearTimeout(timer);
  }, [currentUser, navigateTo]);

  return (
    <div className="min-h-screen bg-[#040711] flex flex-col items-center justify-between p-6 relative overflow-hidden select-none">
      <div className="absolute top-1/4 -right-24 w-96 h-96 bg-[#7C3AED]/20 rounded-full blur-[100px] pointer-events-none" />
      <div className="absolute bottom-1/4 -left-24 w-96 h-96 bg-[#06B6D4]/15 rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full flex justify-end">
        <span className="text-xs font-mono text-[#94A3B8] border border-[#17223B] px-3 py-1 rounded-full bg-[#0A0F1D]/80">
          v1.0.0 SECURE
        </span>
      </div>

      <div className="flex flex-col items-center text-center max-w-sm">
        <div className="relative mb-8">
          <div className="absolute -inset-4 rounded-3xl bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] opacity-40 blur-xl animate-pulse" />
          <div className="relative w-28 h-28 rounded-3xl bg-gradient-to-br from-[#0A0F1D] to-[#111827] border-2 border-[#7C3AED] shadow-[0_0_35px_rgba(124,58,237,0.4)] flex items-center justify-center">
            <Radio className="w-14 h-14 text-[#22D3EE] animate-bounce" />
          </div>
        </div>

        <h1 className="text-4xl font-extrabold tracking-wider font-mono bg-gradient-to-r from-[#F8FAFC] via-[#22D3EE] to-[#8B5CF6] bg-clip-text text-transparent">
          DARK GROUP
        </h1>
        <p className="text-base text-[#94A3B8] font-medium mt-3 leading-relaxed">
          منظومة الاتصال الصوتي الفوري والمشفر مع غرف المحادثة المباشرة
        </p>

        <div className="flex items-center gap-2 mt-6">
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#111827] border border-[#17223B] text-xs text-[#10B981]">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>تشفير طرف لطرف</span>
          </div>
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#111827] border border-[#17223B] text-xs text-[#22D3EE]">
            <Zap className="w-3.5 h-3.5" />
            <span>بث صوتي فوري</span>
          </div>
        </div>
      </div>

      <div className="w-full max-w-xs flex flex-col items-center gap-3">
        <button
          onClick={() => navigateTo(currentUser ? 'DASHBOARD' : 'LANDING')}
          className="w-full py-3 px-4 rounded-xl font-bold bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] text-white shadow-[0_0_20px_rgba(124,58,237,0.4)] hover:brightness-110 active:scale-95 transition-all"
        >
          دخول فوري للمنظومة
        </button>
        <span className="text-[11px] font-mono text-[#64748B]">CYBER NODE NETWORK • ENCRYPTED AUDIO</span>
      </div>
    </div>
  );
};
