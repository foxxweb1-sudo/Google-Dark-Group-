import React from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from './CyberAvatar';
import { PhoneOff, PhoneIncoming, Mic, MicOff, Volume2, VolumeX } from 'lucide-react';

export const CallOverlay: React.FC = () => {
  const {
    callUiState,
    activeCallSession,
    callDurationSec,
    answerCall,
    declineCall,
    endCall,
    currentUser,
    isVoiceMuted,
    toggleVoiceMute,
    isVoiceDeafened,
    toggleVoiceDeafen,
    soundWaveAmplitudes,
  } = useApp();

  if (callUiState === 'IDLE' || !activeCallSession) return null;

  const isIncoming = callUiState === 'INCOMING_RINGING';
  const isOutgoing = callUiState === 'OUTGOING_RINGING';
  const isConnected = callUiState === 'CONNECTED';

  const otherName =
    activeCallSession.callerId === currentUser?.id
      ? activeCallSession.receiverName
      : activeCallSession.callerName;
  const otherUsername =
    activeCallSession.callerId === currentUser?.id
      ? activeCallSession.receiverUsername
      : activeCallSession.callerUsername;
  const otherAvatar =
    activeCallSession.callerId === currentUser?.id
      ? activeCallSession.receiverAvatar
      : activeCallSession.callerAvatar;

  const formatTimer = (sec: number) => {
    const mins = Math.floor(sec / 60);
    const remainingSecs = sec % 60;
    return `${mins.toString().padStart(2, '0')}:${remainingSecs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#040711]/90 backdrop-blur-md">
      <div className="relative w-full max-w-sm rounded-2xl bg-[#0A0F1D] border-2 border-[#17223B] p-6 shadow-[0_0_40px_rgba(124,58,237,0.35)] text-center flex flex-col items-center">
        <div className="absolute -top-12 -left-12 w-32 h-32 bg-[#7C3AED]/20 rounded-full blur-2xl pointer-events-none" />
        <div className="absolute -bottom-12 -right-12 w-32 h-32 bg-[#06B6D4]/20 rounded-full blur-2xl pointer-events-none" />

        <div className="mb-4 inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#111827] border border-[#17223B]">
          <span
            className={`w-2 h-2 rounded-full ${
              isConnected
                ? 'bg-[#10B981] animate-ping'
                : 'bg-[#7C3AED] animate-pulse'
            }`}
          />
          <span className="text-xs font-mono text-[#94A3B8]">
            {isIncoming
              ? 'مكالمة صوتية مشفرة واردة'
              : isOutgoing
              ? 'جارٍ الاتصال المشفر...'
              : 'مكالمة متصلة ومشفرة'}
          </span>
        </div>

        <div className="relative my-4">
          <div
            className={`absolute -inset-3 rounded-full border-2 border-[#7C3AED]/40 ${
              isIncoming || isOutgoing ? 'animate-ping' : ''
            }`}
          />
          <CyberAvatar url={otherAvatar} name={otherName} size="xl" isOnline={true} />
        </div>

        <h3 className="text-xl font-bold text-[#F8FAFC] mt-2">{otherName}</h3>
        <p className="text-xs font-mono text-[#06B6D4] mt-0.5">@{otherUsername}</p>

        {isConnected && (
          <div className="my-5 w-full">
            <p className="text-lg font-mono text-[#10B981] font-semibold">
              {formatTimer(callDurationSec)}
            </p>
            <div className="flex items-center justify-center gap-1.5 h-8 mt-2">
              {soundWaveAmplitudes.map((amp: number, idx: number) => (
                <div
                  key={idx}
                  className="w-1.5 bg-gradient-to-t from-[#7C3AED] to-[#22D3EE] rounded-full transition-all duration-100"
                  style={{ height: `${Math.max(6, amp * 28)}px` }}
                />
              ))}
            </div>
          </div>
        )}

        {isIncoming && (
          <div className="mt-8 flex items-center justify-center gap-8 w-full">
            <button
              onClick={declineCall}
              className="flex flex-col items-center gap-2 text-xs text-[#EF4444] transition-transform hover:scale-110"
              title="رفض"
            >
              <div className="w-14 h-14 rounded-full bg-[#EF4444] text-white flex items-center justify-center shadow-[0_0_15px_#EF4444]">
                <PhoneOff className="w-6 h-6" />
              </div>
              رفض
            </button>

            <button
              onClick={answerCall}
              className="flex flex-col items-center gap-2 text-xs text-[#10B981] transition-transform hover:scale-110"
              title="رد"
            >
              <div className="w-14 h-14 rounded-full bg-[#10B981] text-white flex items-center justify-center shadow-[0_0_15px_#10B981] animate-bounce">
                <PhoneIncoming className="w-6 h-6" />
              </div>
              رد
            </button>
          </div>
        )}

        {isOutgoing && (
          <div className="mt-8 flex flex-col items-center gap-4">
            <p className="text-xs text-[#94A3B8]">يرن الهاتف لدى الطرف الآخر...</p>
            <button
              onClick={endCall}
              className="w-14 h-14 rounded-full bg-[#EF4444] text-white flex items-center justify-center shadow-[0_0_15px_#EF4444] transition-transform hover:scale-110"
              title="إنهاء الاتصال"
            >
              <PhoneOff className="w-6 h-6" />
            </button>
          </div>
        )}

        {isConnected && (
          <div className="mt-6 flex items-center justify-center gap-6 w-full">
            <button
              onClick={toggleVoiceMute}
              className={`p-3 rounded-full border transition-colors ${
                isVoiceMuted
                  ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#EF4444]'
                  : 'bg-[#111827] border-[#17223B] text-[#94A3B8] hover:text-white'
              }`}
              title={isVoiceMuted ? 'إلغاء كتم المايك' : 'كتم المايك'}
            >
              {isVoiceMuted ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
            </button>

            <button
              onClick={endCall}
              className="w-14 h-14 rounded-full bg-[#EF4444] text-white flex items-center justify-center shadow-[0_0_15px_#EF4444] transition-transform hover:scale-110"
              title="إنهاء المكالمة"
            >
              <PhoneOff className="w-6 h-6" />
            </button>

            <button
              onClick={toggleVoiceDeafen}
              className={`p-3 rounded-full border transition-colors ${
                isVoiceDeafened
                  ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#EF4444]'
                  : 'bg-[#111827] border-[#17223B] text-[#94A3B8] hover:text-white'
              }`}
              title={isVoiceDeafened ? 'تشغيل الصوت' : 'كتم السماعة'}
            >
              {isVoiceDeafened ? <VolumeX className="w-5 h-5" /> : <Volume2 className="w-5 h-5" />}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
