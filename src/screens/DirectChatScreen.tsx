import React, { useState, useEffect, useRef } from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from '../components/CyberAvatar';
import { PrivateChatMessage } from '../types';
import { VoiceRecorder } from '../utils/audio';
import {
  ArrowRight,
  Phone,
  Send,
  Mic,
  MicOff,
  Smile,
  Play,
  Pause,
  Trash2,
  ShieldAlert,
  MoreVertical,
  Check,
  User,
} from 'lucide-react';

const QUICK_EMOJIS = ['😀', '😂', '🔥', '👑', '🎙️', '🎧', '❤️', '👏', '👍', '⚡', '🚀', '💯', '😎', '🤝'];

export const DirectChatScreen: React.FC = () => {
  const {
    activeDirectUser,
    privateMessages,
    currentUser,
    friends,
    sendPrivateTextMessage,
    sendPrivateVoiceMessage,
    deletePrivateMessage,
    startCall,
    toggleBlockFriend,
    viewUserProfile,
    navigateTo,
    showToast,
  } = useApp();

  const [text, setText] = useState('');
  const [showEmojiBar, setShowEmojiBar] = useState(false);
  const [selectedMessageForDelete, setSelectedMessageForDelete] = useState<PrivateChatMessage | null>(null);

  // Voice recording state
  const recorderRef = useRef<VoiceRecorder | null>(null);
  const [isRecording, setIsRecording] = useState(false);
  const [recordTimerSec, setRecordTimerSec] = useState(0);
  const recordIntervalRef = useRef<number | null>(null);

  // Voice playback state
  const [playingMsgId, setPlayingMsgId] = useState<string | null>(null);
  const activeAudioRef = useRef<HTMLAudioElement | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const isBlocked = friends.find((f) => f.userId === activeDirectUser?.id)?.isBlocked || false;

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [privateMessages]);

  if (!activeDirectUser) {
    return (
      <div className="min-h-screen bg-[#040711] flex items-center justify-center p-4">
        <button
          onClick={() => navigateTo('PRIVATE_CHATS')}
          className="px-4 py-2 rounded-xl bg-[#7C3AED] text-white"
        >
          العودة للمحادثات الخاصة
        </button>
      </div>
    );
  }

  const handleSendText = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!text.trim() || isBlocked) return;
    await sendPrivateTextMessage(text);
    setText('');
  };

  const handleStartRecording = async () => {
    if (isBlocked) return;
    const rec = new VoiceRecorder();
    recorderRef.current = rec;
    const ok = await rec.start();
    if (ok) {
      setIsRecording(true);
      setRecordTimerSec(0);
      recordIntervalRef.current = window.setInterval(() => {
        setRecordTimerSec((s) => s + 1);
      }, 1000);
    } else {
      showToast('تعذر تشغيل الميكروفون للتسجيل', 'ERROR');
    }
  };

  const handleStopAndSendRecording = async () => {
    if (!recorderRef.current || !isRecording) return;
    if (recordIntervalRef.current) {
      clearInterval(recordIntervalRef.current);
      recordIntervalRef.current = null;
    }
    setIsRecording(false);
    const { blobUrl, durationSec } = await recorderRef.current.stop();
    await sendPrivateVoiceMessage(blobUrl, durationSec);
  };

  const handleCancelRecording = async () => {
    if (recordIntervalRef.current) {
      clearInterval(recordIntervalRef.current);
      recordIntervalRef.current = null;
    }
    setIsRecording(false);
    if (recorderRef.current) {
      await recorderRef.current.stop();
    }
  };

  const handlePlayVoice = (msg: PrivateChatMessage) => {
    if (playingMsgId === msg.id) {
      activeAudioRef.current?.pause();
      setPlayingMsgId(null);
      return;
    }

    if (activeAudioRef.current) {
      activeAudioRef.current.pause();
    }

    if (msg.audioBlobUrl) {
      const audio = new Audio(msg.audioBlobUrl);
      activeAudioRef.current = audio;
      setPlayingMsgId(msg.id);
      audio.play().catch(() => {
        showToast('تعذر تشغيل التسجيل الصوتي', 'ERROR');
        setPlayingMsgId(null);
      });
      audio.onended = () => {
        setPlayingMsgId(null);
      };
    } else {
      // Simulate playback
      setPlayingMsgId(msg.id);
      setTimeout(() => {
        setPlayingMsgId(null);
      }, (msg.voiceDurationSec || 3) * 1000);
    }
  };

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC] flex flex-col h-screen overflow-hidden">
      {/* Top Header */}
      <header className="bg-[#0A0F1D] border-b border-[#17223B] px-4 py-3 shrink-0 flex items-center justify-between z-20">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigateTo('PRIVATE_CHATS')}
            className="p-1.5 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#111827] transition-colors"
          >
            <ArrowRight className="w-5 h-5" />
          </button>

          <div
            onClick={() => viewUserProfile(activeDirectUser)}
            className="flex items-center gap-2.5 cursor-pointer hover:opacity-85 transition-opacity"
          >
            <CyberAvatar
              url={activeDirectUser.avatarUrl}
              name={activeDirectUser.name}
              size="sm"
              isOnline={!isBlocked}
            />
            <div>
              <h3 className="text-sm font-bold text-[#F8FAFC] truncate max-w-[150px] sm:max-w-xs">
                {activeDirectUser.name}
              </h3>
              <p className="text-[11px] font-mono text-[#06B6D4]">@{activeDirectUser.username}</p>
            </div>
          </div>
        </div>

        {/* Call & Profile Actions */}
        <div className="flex items-center gap-2">
          {!isBlocked && (
            <button
              onClick={() => startCall(activeDirectUser)}
              className="p-2 rounded-xl bg-[#10B981]/15 text-[#10B981] hover:bg-[#10B981]/25 border border-[#10B981]/30 transition-colors"
              title="اتصال صوتي مشفر"
            >
              <Phone className="w-4 h-4" />
            </button>
          )}

          <button
            onClick={() => toggleBlockFriend(activeDirectUser.id)}
            className={`p-2 rounded-xl border transition-colors ${
              isBlocked
                ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#EF4444]'
                : 'text-[#94A3B8] hover:text-white border-[#17223B]'
            }`}
            title={isBlocked ? 'إلغاء الحظر' : 'حظر المستخدم'}
          >
            <ShieldAlert className="w-4 h-4" />
          </button>
        </div>
      </header>

      {/* Block warning */}
      {isBlocked && (
        <div className="bg-[#EF4444]/15 border-b border-[#EF4444]/30 px-4 py-2 text-center text-xs text-[#EF4444] font-medium">
          تم حظر هذا المستخدم. لن تتمكن من إرسال أو استقبال رسائل جديدة.
        </div>
      )}

      {/* Messages Feed */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {privateMessages.map((msg) => {
          const isMe = msg.senderId === currentUser?.id || msg.isMe;

          return (
            <div
              key={msg.id}
              className={`flex items-end gap-2 group ${isMe ? 'flex-row-reverse' : 'flex-row'}`}
            >
              <CyberAvatar
                url={isMe ? currentUser?.avatarUrl : activeDirectUser.avatarUrl}
                name={msg.senderName}
                size="xs"
              />

              <div className="relative">
                <div
                  onClick={() => setSelectedMessageForDelete(msg)}
                  className={`rounded-2xl px-4 py-2.5 text-sm shadow-md cursor-pointer transition-all ${
                    isMe
                      ? 'bg-gradient-to-r from-[#7C3AED] to-[#8B5CF6] text-white rounded-br-none'
                      : 'bg-[#111827] border border-[#17223B] text-[#F8FAFC] rounded-bl-none'
                  }`}
                >
                  {/* Voice message render */}
                  {msg.isVoice ? (
                    <div className="flex items-center gap-3 min-w-[160px] py-1">
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          handlePlayVoice(msg);
                        }}
                        className={`w-9 h-9 rounded-full flex items-center justify-center transition-colors ${
                          playingMsgId === msg.id
                            ? 'bg-[#10B981] text-white'
                            : isMe
                            ? 'bg-white/20 hover:bg-white/30 text-white'
                            : 'bg-[#7C3AED] hover:bg-[#8B5CF6] text-white'
                        }`}
                      >
                        {playingMsgId === msg.id ? (
                          <Pause className="w-4 h-4" />
                        ) : (
                          <Play className="w-4 h-4 ms-0.5" />
                        )}
                      </button>

                      <div className="flex-1">
                        <div className="flex items-center gap-1 h-4">
                          {[40, 70, 30, 90, 60, 80, 45, 65].map((h, i) => (
                            <div
                              key={i}
                              className={`w-1 rounded-full ${
                                playingMsgId === msg.id
                                  ? 'bg-[#10B981] animate-pulse'
                                  : isMe
                                  ? 'bg-white/60'
                                  : 'bg-[#22D3EE]'
                              }`}
                              style={{ height: `${(h * 16) / 100}px` }}
                            />
                          ))}
                        </div>
                        <span className="text-[10px] font-mono mt-0.5 block opacity-80">
                          {msg.voiceDurationSec || 2} ثانية • صوت مشفر
                        </span>
                      </div>
                    </div>
                  ) : (
                    <p className="break-words leading-relaxed text-xs sm:text-sm">{msg.text}</p>
                  )}

                  <span
                    className={`block text-[10px] mt-1 text-end ${
                      isMe ? 'text-white/70' : 'text-[#64748B]'
                    }`}
                  >
                    {msg.time}
                  </span>
                </div>
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
              onClick={() => setText((p) => p + emoji)}
              className="text-lg hover:scale-125 transition-transform p-1 shrink-0"
            >
              {emoji}
            </button>
          ))}
        </div>
      )}

      {/* Voice Recording Active Bar */}
      {isRecording && (
        <div className="bg-[#111827] border-t border-[#17223B] px-4 py-3 flex items-center justify-between shrink-0 animate-pulse">
          <div className="flex items-center gap-3">
            <span className="w-3 h-3 rounded-full bg-[#EF4444] animate-ping" />
            <span className="text-xs font-mono text-[#EF4444] font-bold">
              تسجيل صوتي مشفر: 00:{recordTimerSec.toString().padStart(2, '0')}
            </span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleCancelRecording}
              className="px-3 py-1.5 rounded-lg text-xs text-[#94A3B8] hover:text-white hover:bg-[#0A0F1D]"
            >
              إلغاء
            </button>
            <button
              onClick={handleStopAndSendRecording}
              className="px-3 py-1.5 rounded-lg text-xs font-bold bg-[#10B981] text-white flex items-center gap-1 shadow-[0_0_10px_#10B981]"
            >
              <Send className="w-3.5 h-3.5 rtl:-scale-x-100" />
              <span>إرسال</span>
            </button>
          </div>
        </div>
      )}

      {/* Input Bar */}
      {!isRecording && !isBlocked && (
        <form
          onSubmit={handleSendText}
          className="bg-[#0A0F1D] border-t border-[#17223B] p-3 shrink-0 flex items-center gap-2"
        >
          <button
            type="button"
            onClick={() => setShowEmojiBar(!showEmojiBar)}
            className={`p-2 rounded-xl transition-colors ${
              showEmojiBar ? 'bg-[#7C3AED] text-white' : 'text-[#94A3B8] hover:text-white hover:bg-[#111827]'
            }`}
            title="رموز تعبيرية"
          >
            <Smile className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={handleStartRecording}
            className="p-2 rounded-xl text-[#10B981] hover:bg-[#10B981]/15 transition-colors"
            title="تسجيل رسالة صوتية"
          >
            <Mic className="w-5 h-5" />
          </button>

          <input
            type="text"
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="اكتب رسالة خاصة مشفرة..."
            className="flex-1 bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-xs sm:text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED]"
          />

          <button
            type="submit"
            disabled={!text.trim()}
            className="p-2.5 rounded-xl bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] text-white hover:brightness-110 active:scale-95 transition-all disabled:opacity-40 disabled:pointer-events-none shadow-[0_0_12px_rgba(124,58,237,0.3)] cursor-pointer"
            title="إرسال"
          >
            <Send className="w-4 h-4 rtl:-scale-x-100" />
          </button>
        </form>
      )}

      {/* Delete Message Dialog */}
      {selectedMessageForDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#0A0F1D] border border-[#17223B] p-6 shadow-2xl text-center">
            <Trash2 className="w-8 h-8 text-[#EF4444] mx-auto mb-2" />
            <h3 className="text-sm font-bold text-[#F8FAFC]">خيارات حذف الرسالة</h3>
            <p className="text-xs text-[#94A3B8] my-3">
              هل ترغب بحذف هذه الرسالة من المحادثة؟
            </p>

            <div className="space-y-2 mt-4">
              <button
                onClick={async () => {
                  const m = selectedMessageForDelete;
                  setSelectedMessageForDelete(null);
                  await deletePrivateMessage(m.id, false);
                }}
                className="w-full py-2.5 rounded-xl text-xs font-bold bg-[#111827] border border-[#17223B] text-[#F8FAFC] hover:bg-[#1e293b]"
              >
                حذف لدي فقط
              </button>

              {(selectedMessageForDelete.senderId === currentUser?.id || selectedMessageForDelete.isMe) && (
                <button
                  onClick={async () => {
                    const m = selectedMessageForDelete;
                    setSelectedMessageForDelete(null);
                    await deletePrivateMessage(m.id, true);
                  }}
                  className="w-full py-2.5 rounded-xl text-xs font-bold bg-[#EF4444] text-white hover:bg-red-600"
                >
                  حذف لدى الجميع
                </button>
              )}

              <button
                onClick={() => setSelectedMessageForDelete(null)}
                className="w-full py-2 rounded-xl text-xs text-[#94A3B8] hover:text-white"
              >
                إلغاء
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
