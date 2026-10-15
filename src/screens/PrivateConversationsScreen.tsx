import React from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from '../components/CyberAvatar';
import { ArrowRight, MessageSquare, Plus, Mic, Shield } from 'lucide-react';

export const PrivateConversationsScreen: React.FC = () => {
  const { conversations, openDirectChat, navigateTo, friends } = useApp();

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC] flex flex-col pb-12">
      {/* Top Header */}
      <header className="sticky top-0 z-30 bg-[#0A0F1D]/90 backdrop-blur-md border-b border-[#17223B] px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigateTo('DASHBOARD')}
            className="p-1.5 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#111827] transition-colors"
          >
            <ArrowRight className="w-5 h-5" />
          </button>
          <div>
            <h2 className="text-base font-bold text-[#F8FAFC]">المحادثات الخاصة المشفرة</h2>
            <p className="text-[11px] text-[#94A3B8]">محادثات مباشرة طرف لطيف مع دعم الرسائل الصوتية</p>
          </div>
        </div>

        <button
          onClick={() => navigateTo('FRIENDS')}
          className="px-3 py-1.5 rounded-xl text-xs font-bold bg-[#7C3AED] hover:bg-[#8B5CF6] text-white flex items-center gap-1.5 transition-colors shadow-[0_0_10px_rgba(124,58,237,0.3)]"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>محادثة جديدة</span>
        </button>
      </header>

      <main className="max-w-xl mx-auto w-full px-4 pt-4 space-y-2">
        {conversations.length === 0 ? (
          <div className="p-8 text-center bg-[#0A0F1D] border border-[#17223B] rounded-2xl mt-4">
            <MessageSquare className="w-10 h-10 text-[#64748B] mx-auto mb-2" />
            <h3 className="text-sm font-bold text-[#F8FAFC]">لا توجد محادثات خاصة بعد</h3>
            <p className="text-xs text-[#94A3B8] mt-1 mb-4">
              اختر صديقاً من قائمة الأصدقاء لبدء محادثة مشفرة وفورية
            </p>
            <button
              onClick={() => navigateTo('FRIENDS')}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-[#7C3AED] text-white"
            >
              الذهاب للأصدقاء
            </button>
          </div>
        ) : (
          conversations.map((conv) => (
            <div
              key={conv.id}
              onClick={() =>
                openDirectChat({
                  id: conv.otherUserId,
                  username: conv.otherUsername,
                  name: conv.otherName,
                  avatarUrl: conv.otherAvatarUrl,
                  createdAt: Date.now(),
                })
              }
              className="p-3.5 rounded-xl bg-[#0A0F1D] border border-[#17223B] hover:border-[#7C3AED]/60 hover:bg-[#111827] transition-all flex items-center justify-between gap-3 cursor-pointer group"
            >
              <div className="flex items-center gap-3 min-w-0">
                <CyberAvatar
                  url={conv.otherAvatarUrl}
                  name={conv.otherName}
                  size="md"
                  isOnline={true}
                />

                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <h4 className="text-xs sm:text-sm font-bold text-[#F8FAFC] group-hover:text-[#22D3EE] transition-colors truncate">
                      {conv.otherName}
                    </h4>
                  </div>
                  <p className="text-[11px] font-mono text-[#06B6D4] truncate">@{conv.otherUsername}</p>
                  <p className="text-xs text-[#94A3B8] truncate mt-0.5 flex items-center gap-1">
                    {conv.lastMessage.includes('صوتية') && <Mic className="w-3 h-3 text-[#10B981] shrink-0" />}
                    <span>{conv.lastMessage || 'بدء المحادثة المشفرة'}</span>
                  </p>
                </div>
              </div>

              <div className="shrink-0 text-end">
                <span className="text-[10px] text-[#64748B] block">{conv.lastMessageTime}</span>
              </div>
            </div>
          ))
        )}
      </main>
    </div>
  );
};
