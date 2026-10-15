import React, { useState, useEffect, useRef } from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from '../components/CyberAvatar';
import { ArrowRight, Radio, Send, Smile, Sparkles, MessageCircle } from 'lucide-react';

const QUICK_EMOJIS = ['🔥', '⚡', '👑', '🚀', '💯', '🎙️', '🎧', '😀', '👏', '🤝'];

export const ForumScreen: React.FC = () => {
  const { forumMessages, sendForumMessage, navigateTo, viewUserProfile, currentUser } = useApp();
  const [postText, setPostText] = useState('');
  const [showEmojiBar, setShowEmojiBar] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [forumMessages]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!postText.trim()) return;
    await sendForumMessage(postText);
    setPostText('');
  };

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC] flex flex-col h-screen overflow-hidden">
      {/* Top Header */}
      <header className="bg-[#0A0F1D] border-b border-[#17223B] px-4 py-3 shrink-0 flex items-center justify-between z-20">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigateTo('DASHBOARD')}
            className="p-1.5 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#111827] transition-colors"
          >
            <ArrowRight className="w-5 h-5" />
          </button>
          <div>
            <h2 className="text-base font-bold text-[#F8FAFC] flex items-center gap-2">
              <Radio className="w-4 h-4 text-[#F59E0B]" />
              <span>المنتدى العام لشبكة Dark Group</span>
            </h2>
            <p className="text-[11px] text-[#94A3B8]">نقاشات المجتمع العام والتقنيات الصوتية المشفرة</p>
          </div>
        </div>
      </header>

      {/* Forum Posts List */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 max-w-2xl mx-auto w-full">
        {forumMessages.map((msg) => (
          <div
            key={msg.id}
            className="p-4 rounded-2xl bg-[#0A0F1D] border border-[#17223B] shadow-md hover:border-[#7C3AED]/40 transition-colors"
          >
            <div className="flex items-center justify-between mb-3">
              <div
                onClick={() =>
                  viewUserProfile({
                    id: msg.senderId,
                    username: msg.senderName.toLowerCase().replace(/[^a-z0-9_]/g, '') || 'user',
                    name: msg.senderName,
                    avatarUrl: msg.avatarUrl,
                    createdAt: msg.timestamp,
                  })
                }
                className="flex items-center gap-2.5 cursor-pointer hover:opacity-85"
              >
                <CyberAvatar
                  url={msg.avatarUrl}
                  name={msg.senderName}
                  size="sm"
                  isOnline={true}
                />
                <div>
                  <h4 className="text-xs sm:text-sm font-bold text-[#F8FAFC]">{msg.senderName}</h4>
                  <span className="text-[10px] text-[#64748B]">{msg.time}</span>
                </div>
              </div>

              <span className="text-[10px] font-mono text-[#06B6D4] bg-[#111827] px-2 py-0.5 rounded border border-[#17223B]">
                منشور عام
              </span>
            </div>

            <p className="text-xs sm:text-sm text-[#F8FAFC] leading-relaxed whitespace-pre-wrap">
              {msg.text}
            </p>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Quick Emojis Drawer */}
      {showEmojiBar && (
        <div className="bg-[#0A0F1D] border-t border-[#17223B] px-3 py-2 flex items-center gap-2 overflow-x-auto shrink-0 max-w-2xl mx-auto w-full">
          {QUICK_EMOJIS.map((emoji) => (
            <button
              key={emoji}
              onClick={() => setPostText((p) => p + emoji)}
              className="text-lg hover:scale-125 transition-transform p-1 shrink-0"
            >
              {emoji}
            </button>
          ))}
        </div>
      )}

      {/* Forum Input Bar */}
      <form
        onSubmit={handleSend}
        className="bg-[#0A0F1D] border-t border-[#17223B] p-3 shrink-0 flex items-center gap-2 max-w-2xl mx-auto w-full"
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

        <input
          type="text"
          value={postText}
          onChange={(e) => setPostText(e.target.value)}
          placeholder="شارك بموضوع أو استفسار في المنتدى العام..."
          className="flex-1 bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-xs sm:text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#F59E0B]"
        />

        <button
          type="submit"
          disabled={!postText.trim()}
          className="p-2.5 rounded-xl bg-gradient-to-r from-[#F59E0B] to-[#EF4444] text-[#040711] font-bold hover:brightness-110 active:scale-95 transition-all disabled:opacity-40 disabled:pointer-events-none cursor-pointer"
          title="نشر"
        >
          <Send className="w-4 h-4 rtl:-scale-x-100 text-white" />
        </button>
      </form>
    </div>
  );
};
