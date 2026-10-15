import React from 'react';
import { useApp } from '../context/AppContext';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';
import { ToastEvent } from '../types';

export const ToastContainer: React.FC = () => {
  const { toasts, dismissToast } = useApp();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 flex flex-col items-center gap-2 max-w-md w-full px-4 pointer-events-none">
      {toasts.map((toast: ToastEvent) => {
        let borderClass = 'border-[#7C3AED]/40 bg-[#0A0F1D]/95 text-[#F8FAFC]';
        let icon = <Info className="w-5 h-5 text-[#22D3EE] shrink-0" />;

        if (toast.type === 'SUCCESS') {
          borderClass = 'border-[#10B981]/50 bg-[#0A0F1D]/95 text-[#F8FAFC]';
          icon = <CheckCircle2 className="w-5 h-5 text-[#10B981] shrink-0" />;
        } else if (toast.type === 'ERROR') {
          borderClass = 'border-[#EF4444]/50 bg-[#0A0F1D]/95 text-[#F8FAFC]';
          icon = <AlertCircle className="w-5 h-5 text-[#EF4444] shrink-0" />;
        }

        return (
          <div
            key={toast.id}
            className={`pointer-events-auto flex items-center justify-between gap-3 px-4 py-3 rounded-xl border backdrop-blur-md shadow-[0_4px_20px_rgba(0,0,0,0.5)] transition-all animate-in fade-in slide-in-from-top-2 w-full ${borderClass}`}
          >
            <div className="flex items-center gap-2.5 text-sm font-medium">
              {icon}
              <span>{toast.message}</span>
            </div>
            <button
              onClick={() => dismissToast(toast.id)}
              className="p-1 text-[#94A3B8] hover:text-white transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        );
      })}
    </div>
  );
};
