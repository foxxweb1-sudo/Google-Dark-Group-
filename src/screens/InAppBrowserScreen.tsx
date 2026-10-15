import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { X, ArrowRight, ExternalLink, RefreshCw, Shield, Globe } from 'lucide-react';

export const InAppBrowserScreen: React.FC = () => {
  const { browserUrl, browserTitle, closeInAppBrowser } = useApp();
  const [iframeLoaded, setIframeLoaded] = useState(false);
  const [hasError, setHasError] = useState(false);

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC] flex flex-col h-screen overflow-hidden">
      {/* Browser Bar */}
      <header className="bg-[#0A0F1D] border-b border-[#17223B] px-4 py-3 shrink-0 flex items-center justify-between z-20">
        <div className="flex items-center gap-3 min-w-0">
          <button
            onClick={closeInAppBrowser}
            className="p-1.5 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#111827] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>

          <div className="min-w-0">
            <h3 className="text-xs sm:text-sm font-bold text-[#F8FAFC] truncate">
              {browserTitle}
            </h3>
            <p className="text-[11px] font-mono text-[#06B6D4] truncate">{browserUrl}</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <a
            href={browserUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="p-2 rounded-xl text-[#94A3B8] hover:text-[#22D3EE] hover:bg-[#111827] transition-colors"
            title="فتح في نافذة خارجية"
          >
            <ExternalLink className="w-4 h-4" />
          </a>
        </div>
      </header>

      {/* Embedded View with Fallback */}
      <div className="flex-1 bg-[#0A0F1D] flex flex-col relative">
        <iframe
          src={browserUrl}
          title={browserTitle}
          className="w-full h-full border-none"
          onLoad={() => setIframeLoaded(true)}
          onError={() => setHasError(true)}
          sandbox="allow-scripts allow-same-origin allow-forms"
        />

        {/* Fallback card if external website blocks iframe embedding */}
        <div className="absolute inset-0 bg-[#0A0F1D] flex flex-col items-center justify-center p-6 text-center pointer-events-none opacity-0 hover:opacity-100 transition-opacity">
          <div className="pointer-events-auto max-w-sm p-6 rounded-2xl bg-[#111827] border border-[#17223B] shadow-2xl">
            <Globe className="w-12 h-12 text-[#22D3EE] mx-auto mb-3" />
            <h4 className="text-sm font-bold text-[#F8FAFC]">{browserTitle}</h4>
            <p className="text-xs text-[#94A3B8] my-3 leading-relaxed">
              إذا كانت صفحة الويب تمنع العرض داخل الإطار، يمكنك فتح الرابط مباشرة في المتصفح.
            </p>
            <a
              href={browserUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold bg-[#7C3AED] hover:bg-[#8B5CF6] text-white shadow-[0_0_15px_rgba(124,58,237,0.4)] transition-all"
            >
              <span>فتح الرابط المباشر</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};
