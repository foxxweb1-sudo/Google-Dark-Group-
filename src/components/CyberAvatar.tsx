import React from 'react';

interface CyberAvatarProps {
  url?: string | null;
  name?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  isOnline?: boolean;
  isSpeaking?: boolean;
  className?: string;
}

export const CyberAvatar: React.FC<CyberAvatarProps> = ({
  url,
  name = 'مستخدم',
  size = 'md',
  isOnline,
  isSpeaking,
  className = '',
}) => {
  const sizeMap = {
    xs: 'w-7 h-7 text-xs',
    sm: 'w-9 h-9 text-sm',
    md: 'w-12 h-12 text-base',
    lg: 'w-16 h-16 text-xl',
    xl: 'w-24 h-24 text-2xl',
  };

  const initial = (name.trim()[0] || 'D').toUpperCase();

  return (
    <div className={`relative inline-flex items-center justify-center shrink-0 ${sizeMap[size]} ${className}`}>
      <div
        className={`w-full h-full rounded-full overflow-hidden flex items-center justify-center border-2 transition-all duration-300 ${
          isSpeaking
            ? 'border-[#10B981] shadow-[0_0_15px_#10B981]'
            : 'border-[#7C3AED]/70 hover:border-[#22D3EE] shadow-[0_0_8px_rgba(124,58,237,0.3)]'
        } bg-[#0A0F1D]`}
      >
        {url ? (
          <img
            src={url}
            alt={name}
            className="w-full h-full object-cover"
            onError={(e) => {
              (e.target as HTMLElement).style.display = 'none';
            }}
          />
        ) : (
          <span className="font-bold font-mono text-[#F8FAFC] tracking-wider select-none">
            {initial}
          </span>
        )}
      </div>

      {isOnline !== undefined && (
        <span
          className={`absolute bottom-0 end-0 rounded-full border-2 border-[#040711] ${
            size === 'sm' || size === 'xs' ? 'w-2.5 h-2.5' : 'w-3.5 h-3.5'
          } ${isOnline ? 'bg-[#10B981] shadow-[0_0_8px_#10B981]' : 'bg-[#64748B]'}`}
        />
      )}
    </div>
  );
};
