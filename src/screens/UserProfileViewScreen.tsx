import React from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from '../components/CyberAvatar';
import {
  ArrowRight,
  Phone,
  MessageSquare,
  UserPlus,
  ShieldAlert,
  Radio,
  Check,
} from 'lucide-react';

export const UserProfileViewScreen: React.FC = () => {
  const {
    selectedUserProfile,
    currentUser,
    friends,
    startCall,
    openDirectChat,
    sendFriendRequest,
    toggleBlockFriend,
    navigateTo,
  } = useApp();

  if (!selectedUserProfile) {
    return (
      <div className="min-h-screen bg-[#040711] flex items-center justify-center p-4">
        <button
          onClick={() => navigateTo('DASHBOARD')}
          className="px-4 py-2 rounded-xl bg-[#7C3AED] text-white"
        >
          العودة للوحة التحكم
        </button>
      </div>
    );
  }

  const isMe = selectedUserProfile.id === currentUser?.id;
  const friendRecord = friends.find((f) => f.userId === selectedUserProfile.id);
  const isFriend = !!friendRecord;
  const isBlocked = friendRecord?.isBlocked || false;

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
          <h2 className="text-base font-bold text-[#F8FAFC]">بطاقة المستخدم المشفرة</h2>
        </div>
      </header>

      <main className="max-w-md mx-auto w-full px-4 pt-8 space-y-6">
        <div className="p-6 rounded-2xl bg-[#0A0F1D] border border-[#17223B] text-center flex flex-col items-center shadow-xl relative overflow-hidden">
          <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-[#7C3AED] to-[#06B6D4]" />

          <div className="my-3">
            <CyberAvatar
              url={selectedUserProfile.avatarUrl}
              name={selectedUserProfile.name}
              size="xl"
              isOnline={!isBlocked}
            />
          </div>

          <h3 className="text-xl font-bold text-[#F8FAFC]">{selectedUserProfile.name}</h3>
          <p className="text-xs font-mono text-[#06B6D4] mt-0.5">@{selectedUserProfile.username}</p>

          {selectedUserProfile.isAnon && (
            <span className="mt-2 text-[10px] px-2.5 py-0.5 rounded-full bg-[#7C3AED]/20 text-[#8B5CF6] border border-[#7C3AED]/40">
              مستخدم في وضع التخفي
            </span>
          )}

          {/* Action buttons if not me */}
          {!isMe && (
            <div className="w-full mt-6 space-y-3">
              {!isBlocked && (
                <div className="flex gap-2">
                  <button
                    onClick={() => startCall(selectedUserProfile)}
                    className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-[#10B981] hover:bg-emerald-600 text-white flex items-center justify-center gap-2 shadow-[0_0_12px_#10B981] transition-all"
                  >
                    <Phone className="w-4 h-4" />
                    <span>مكالمة صوتية</span>
                  </button>

                  <button
                    onClick={() => openDirectChat(selectedUserProfile)}
                    className="flex-1 py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-[#7C3AED] to-[#8B5CF6] text-white flex items-center justify-center gap-2 shadow-[0_0_12px_rgba(124,58,237,0.3)] transition-all"
                  >
                    <MessageSquare className="w-4 h-4" />
                    <span>محادثة خاصة</span>
                  </button>
                </div>
              )}

              {!isFriend && (
                <button
                  onClick={() => sendFriendRequest(selectedUserProfile.username)}
                  className="w-full py-2.5 rounded-xl text-xs font-bold bg-[#111827] border border-[#17223B] hover:border-[#7C3AED] text-[#22D3EE] flex items-center justify-center gap-2 transition-colors"
                >
                  <UserPlus className="w-4 h-4" />
                  <span>إرسال طلب صداقة</span>
                </button>
              )}

              {isFriend && (
                <div className="py-2 px-3 rounded-xl bg-[#111827] border border-[#17223B] text-xs text-[#10B981] flex items-center justify-center gap-1.5 font-bold">
                  <Check className="w-4 h-4" />
                  <span>صديق في شبكتك المشفرة</span>
                </div>
              )}

              <button
                onClick={() => toggleBlockFriend(selectedUserProfile.id)}
                className={`w-full py-2 rounded-xl text-xs font-bold border transition-colors flex items-center justify-center gap-2 ${
                  isBlocked
                    ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#EF4444]'
                    : 'bg-[#111827] border-[#17223B] text-[#94A3B8] hover:text-[#EF4444]'
                }`}
              >
                <ShieldAlert className="w-4 h-4" />
                <span>{isBlocked ? 'إلغاء حظر المستخدم' : 'حظر هذا المستخدم'}</span>
              </button>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};
