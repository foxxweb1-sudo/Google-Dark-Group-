import React, { useState, useEffect, useRef } from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from '../components/CyberAvatar';
import {
  ArrowRight,
  Copy,
  Trash2,
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  PhoneCall,
  PhoneOff,
  Send,
  Smile,
  Shield,
  Lock,
  Unlock,
  Radio,
  Check,
} from 'lucide-react';

const QUICK_EMOJIS = ['😀', '😂', '🔥', '👑', '🎙️', '🎧', '❤️', '👏', '👍', '⚡', '🚀', '💯', '😎', '🤝'];

export const ActiveRoomScreen: React.FC = () => {
  const {
    currentRoom,
    currentUser,
    roomUsers,
    roomMessages,
    leaveRoom,
    deleteCurrentRoom,
    sendRoomMessage,
    isVoiceConnected,
    isVoiceMuted,
    isVoiceDeafened,
    isVoiceSpeaking,
    soundWaveAmplitudes,
    toggleVoiceConnection,
    toggleVoiceMute,
    toggleVoiceDeafen,
    viewUserProfile,
    showToast,
  } = useApp();

  const [messageText, setMessageText] = useState('');
  const [showEmojiBar, setShowEmojiBar] = useState(false);
  const [copied, setCopied] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const isHost = currentUser && currentRoom && currentUser.id === currentRoom.hostId;

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [roomMessages]);

  if (!currentRoom) {
    return (
      <div className="min-h-screen bg-[#040711] flex items-center justify-center p-4">
        <button
          onClick={leaveRoom}
          className="px-4 py-2 rounded-xl bg-[#7C3AED] text-white font-bold"
        >
          العودة للوحة التحكم
        </button>
      </div>
    );
  }

  const handleCopyCode = () => {
    navigator.clipboard.writeText(currentRoom.id);
    setCopied(true);
    showToast(`تم نسخ كود الغرفة: ${currentRoom.id}`, 'SUCCESS');
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!messageText.trim()) return;
    await sendRoomMessage(messageText);
    setMessageText('');
  };

  const handleAddEmoji = (emoji: string) => {
    setMessageText((prev) => prev + emoji);
  };

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC] flex flex-col h-screen overflow-hidden">
      {/* Top Header */}
      <header className="bg-[#0A0F1D] border-b border-[#17223B] px-4 py-3 shrink-0 flex items-center justify-between z-20">
        <div className="flex items-center gap-3">
          <button
            onClick={leaveRoom}
            className="p-1.5 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#111827] transition-colors"
            title="مغادرة الغرفة"
          >
            <ArrowRight className="w-5 h-5" />
          </button>

          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-bold text-[#F8FAFC] truncate max-w-[180px] sm:max-w-xs">
                {currentRoom.name}
              </h2>
              {currentRoom.isLocked ? (
                <Lock className="w-3.5 h-3.5 text-[#EF4444] shrink-0" />
              ) : (
                <Unlock className="w-3.5 h-3.5 text-[#10B981] shrink-0" />
              )}
            </div>
            <div className="flex items-center gap-2 text-xs">
              <button
                onClick={handleCopyCode}
                className="flex items-center gap-1 font-mono text-[#06B6D4] hover:text-[#22D3EE] transition-colors"
              >
                <span>{currentRoom.id}</span>
                {copied ? <Check className="w-3 h-3 text-[#10B981]" /> : <Copy className="w-3 h-3" />}
              </button>
              <span className="text-[#64748B]">•</span>
              <span className="text-[#94A3B8] text-[11px]">المنشئ: {currentRoom.host}</span>
            </div>
          </div>
        </div>

        {/* Header Actions */}
        <div className="flex items-center gap-2">
          {isHost && (
            <button
              onClick={() => setShowDeleteModal(true)}
              className="p-2 rounded-xl text-[#EF4444] hover:bg-[#EF4444]/10 transition-colors"
              title="حذف الغرفة"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}

          <button
            onClick={leaveRoom}
            className="px-3 py-1.5 rounded-xl text-xs font-bold bg-[#EF4444]/10 border border-[#EF4444]/30 text-[#EF4444] hover:bg-[#EF4444]/20 transition-colors"
          >
            مغادرة
          </button>
        </div>
      </header>

      {/* Voice Control Strip & Sound Waves */}
      <div className="bg-[#111827] border-b border-[#17223B] px-4 py-2.5 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <button
            onClick={toggleVoiceConnection}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold flex items-center gap-2 transition-all ${
              isVoiceConnected
                ? 'bg-[#10B981] text-white shadow-[0_0_12px_#10B981]'
                : 'bg-[#7C3AED] hover:bg-[#8B5CF6] text-white shadow-[0_0_12px_rgba(124,58,237,0.3)]'
            }`}
          >
            {isVoiceConnected ? (
              <>
                <PhoneOff className="w-3.5 h-3.5" />
                <span>متصل بالصوت</span>
              </>
            ) : (
              <>
                <PhoneCall className="w-3.5 h-3.5" />
                <span>دخول القناة الصوتية</span>
              </>
            )}
          </button>

          {/* Real Animated Waveforms when speaking */}
          {isVoiceConnected && (
            <div className="flex items-center gap-1 h-5 px-2">
              {soundWaveAmplitudes.map((amp, idx) => (
                <div
                  key={idx}
                  className="w-1 bg-gradient-to-t from-[#7C3AED] to-[#22D3EE] rounded-full transition-all duration-75"
                  style={{ height: `${Math.max(4, amp * 20)}px` }}
                />
              ))}
            </div>
          )}
        </div>

        {/* Audio Mute & Deafen Toggles */}
        {isVoiceConnected && (
          <div className="flex items-center gap-2">
            <button
              onClick={toggleVoiceMute}
              className={`p-2 rounded-xl border transition-colors ${
                isVoiceMuted
                  ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#EF4444]'
                  : 'bg-[#0A0F1D] border-[#17223B] text-[#10B981] hover:bg-[#111827]'
              }`}
              title={isVoiceMuted ? 'إلغاء كتم المايك' : 'كتم المايك'}
            >
              {isVoiceMuted ? <MicOff className="w-4 h-4" /> : <Mic className="w-4 h-4" />}
            </button>

            <button
              onClick={toggleVoiceDeafen}
              className={`p-2 rounded-xl border transition-colors ${
                isVoiceDeafened
                  ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#EF4444]'
                  : 'bg-[#0A0F1D] border-[#17223B] text-[#94A3B8] hover:text-white hover:bg-[#111827]'
              }`}
              title={isVoiceDeafened ? 'تشغيل صوت الغرفة' : 'كتم صوت الغرفة'}
            >
              {isVoiceDeafened ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
            </button>
          </div>
        )}
      </div>

      {/* Room Participants Bar */}
      <div className="bg-[#0A0F1D]/80 border-b border-[#17223B] px-4 py-2.5 overflow-x-auto shrink-0 flex items-center gap-4">
        <span className="text-[11px] font-mono text-[#94A3B8] shrink-0">
          المتواجدون ({roomUsers.length}):
        </span>

        <div className="flex items-center gap-3">
          {roomUsers.map((user) => {
            const isMe = user.id === currentUser?.id;
            const isSpeakingNow = isMe ? isVoiceSpeaking : user.isSpeaking;
            const isMutedNow = isMe ? isVoiceMuted : user.isMuted;

            return (
              <div
                key={user.id}
                onClick={() =>
                  viewUserProfile({
                    id: user.id,
                    username: user.username,
                    name: user.name,
                    avatarUrl: user.avatarUrl,
                    createdAt: Date.now(),
                  })
                }
                className="flex items-center gap-2 p-1.5 rounded-xl bg-[#111827] border border-[#17223B] hover:border-[#7C3AED] transition-colors cursor-pointer shrink-0"
                title={`${user.name} (@${user.username})`}
              >
                <div className="relative">
                  <CyberAvatar
                    url={user.avatarUrl}
                    name={user.name}
                    size="sm"
                    isSpeaking={isSpeakingNow}
                  />
                  {user.isVoice && (
                    <span
                      className={`absolute -bottom-1 -end-1 w-3.5 h-3.5 rounded-full flex items-center justify-center border border-[#040711] ${
                        isMutedNow ? 'bg-[#EF4444]' : 'bg-[#10B981]'
                      }`}
                    >
                      {isMutedNow ? (
                        <MicOff className="w-2 h-2 text-white" />
                      ) : (
                        <Mic className="w-2 h-2 text-white" />
                      )}
                    </span>
                  )}
                </div>

                <div className="text-right">
                  <p className="text-xs font-bold text-[#F8FAFC] max-w-[90px] truncate leading-tight">
                    {user.name}
                  </p>
                  <p className="text-[10px] font-mono text-[#06B6D4] leading-tight">
                    {isMe ? 'أنت' : `@${user.username}`}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Live Chat Messages Feed */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {roomMessages.map((msg) => {
          if (msg.isSystem) {
            return (
              <div
                key={msg.id}
                className="flex items-center justify-center my-3"
              >
                <span className="text-[11px] font-mono text-[#22D3EE] bg-[#111827] border border-[#17223B] px-3 py-1 rounded-full text-center">
                  🛡️ {msg.text}
                </span>
              </div>
            );
          }

          const isMe = msg.senderId === currentUser?.id || msg.isMe;

          return (
            <div
              key={msg.id}
              className={`flex items-end gap-2.5 ${isMe ? 'flex-row-reverse' : 'flex-row'}`}
            >
              <CyberAvatar
                url={msg.avatarUrl}
                name={msg.senderName}
                size="xs"
              />

              <div
                className={`max-w-[75%] sm:max-w-md rounded-2xl px-3.5 py-2 text-sm shadow-md ${
                  isMe
                    ? 'bg-gradient-to-r from-[#7C3AED] to-[#8B5CF6] text-white rounded-br-none'
                    : 'bg-[#111827] border border-[#17223B] text-[#F8FAFC] rounded-bl-none'
                }`}
              >
                {!isMe && (
                  <p className="text-[11px] font-bold text-[#22D3EE] mb-0.5">
                    {msg.senderName}
                  </p>
                )}
                <p className="break-words leading-relaxed text-xs sm:text-sm">{msg.text}</p>
                <span
                  className={`block text-[10px] mt-1 text-end ${
                    isMe ? 'text-white/70' : 'text-[#64748B]'
                  }`}
                >
                  {msg.time}
                </span>
              </div>
            </div>
          );
        })}
        <div ref={messagesEndRef} />
      </div>

      {/* Quick Emojis Drawer */}
      {showEmojiBar && (
        <div className="bg-[#0A0F1D] border-t border-[#17223B] px-3 py-2 flex items-center gap-2 overflow-x-auto shrink-0">
          {QUICK_EMOJIS.map((emoji) => (
            <button
              key={emoji}
              onClick={() => handleAddEmoji(emoji)}
              className="text-lg hover:scale-125 transition-transform p-1 shrink-0"
            >
              {emoji}
            </button>
          ))}
        </div>
      )}

      {/* Message Input Bar */}
      <form
        onSubmit={handleSendMessage}
        className="bg-[#0A0F1D] border-t border-[#17223B] p-3 shrink-0 flex items-center gap-2"
      >
        <button
          type="button"
          onClick={() => setShowEmojiBar(!showEmojiBar)}
          className={`p-2 rounded-xl transition-colors ${
            showEmojiBar
              ? 'bg-[#7C3AED] text-white'
              : 'text-[#94A3B8] hover:text-white hover:bg-[#111827]'
          }`}
          title="رموز تعبيرية"
        >
          <Smile className="w-5 h-5" />
        </button>

        <input
          type="text"
          value={messageText}
          onChange={(e) => setMessageText(e.target.value)}
          placeholder="اكتب رسالة مشفرة في الغرفة..."
          className="flex-1 bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-xs sm:text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED]"
        />

        <button
          type="submit"
          disabled={!messageText.trim()}
          className="p-2.5 rounded-xl bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] text-white hover:brightness-110 active:scale-95 transition-all disabled:opacity-40 disabled:pointer-events-none shadow-[0_0_12px_rgba(124,58,237,0.3)] cursor-pointer"
          title="إرسال"
        >
          <Send className="w-4 h-4 rtl:-scale-x-100" />
        </button>
      </form>

      {/* Delete Room Confirmation Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#0A0F1D] border border-[#17223B] p-6 shadow-2xl text-center">
            <Trash2 className="w-10 h-10 text-[#EF4444] mx-auto mb-3" />
            <h3 className="text-base font-bold text-[#F8FAFC]">حذف الغرفة المشفرة نهائياً؟</h3>
            <p className="text-xs text-[#94A3B8] my-3 leading-relaxed">
              سيتم إنهاء الجلسة وإغلاق القناة الصوتية لجميع المشاركين المتواجدين حالياً.
            </p>

            <div className="flex gap-2 mt-4">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-[#111827] text-[#94A3B8] hover:text-white"
              >
                إلغاء
              </button>
              <button
                onClick={async () => {
                  setShowDeleteModal(false);
                  await deleteCurrentRoom();
                }}
                className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-[#EF4444] hover:bg-red-600 text-white"
              >
                تأكيد الحذف
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
