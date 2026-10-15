import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from '../components/CyberAvatar';
import { User, Friend, FriendRequest } from '../types';
import {
  ArrowRight,
  Search,
  UserPlus,
  Check,
  X,
  Phone,
  MessageSquare,
  ShieldAlert,
  Trash2,
  Radio,
  Lock,
  UserCheck,
} from 'lucide-react';

export const FriendsScreen: React.FC = () => {
  const {
    friends,
    friendRequests,
    searchUser,
    isSearchingUser,
    searchUserResult,
    sendFriendRequest,
    acceptFriendRequest,
    declineFriendRequest,
    toggleBlockFriend,
    removeFriend,
    startCall,
    openDirectChat,
    viewUserProfile,
    joinRoom,
    navigateTo,
    currentUser,
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<'friends' | 'requests'>('friends');

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    await searchUser(searchQuery.trim());
  };

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
          <h2 className="text-base font-bold text-[#F8FAFC]">شبكة الأصدقاء والمستخدمين</h2>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-mono text-[#06B6D4] bg-[#111827] px-2.5 py-1 rounded-full border border-[#17223B]">
            {friends.length} صديق
          </span>
        </div>
      </header>

      <main className="max-w-2xl mx-auto w-full px-4 pt-6 space-y-6">
        {/* Search for User Bar */}
        <div className="p-4 rounded-2xl bg-[#0A0F1D] border border-[#17223B] shadow-lg">
          <h3 className="text-xs font-bold text-[#94A3B8] mb-2 flex items-center gap-1.5">
            <Search className="w-3.5 h-3.5 text-[#22D3EE]" />
            <span>البحث عن مستخدم بالاسم الإنجليزي (@username)</span>
          </h3>

          <form onSubmit={handleSearch} className="flex gap-2">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="اكتب اسم المستخدم..."
              className="flex-1 bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2 text-xs sm:text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED]"
            />
            <button
              type="submit"
              disabled={isSearchingUser}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-[#7C3AED] hover:bg-[#8B5CF6] text-white transition-colors flex items-center gap-1.5"
            >
              <Search className="w-3.5 h-3.5" />
              <span>بحث</span>
            </button>
          </form>

          {/* Search result preview */}
          {searchUserResult && (
            <div className="mt-4 p-3 rounded-xl bg-[#111827] border border-[#7C3AED]/40 flex items-center justify-between animate-in fade-in">
              <div
                onClick={() => viewUserProfile(searchUserResult)}
                className="flex items-center gap-3 cursor-pointer"
              >
                <CyberAvatar
                  url={searchUserResult.avatarUrl}
                  name={searchUserResult.name}
                  size="md"
                />
                <div>
                  <h4 className="text-xs sm:text-sm font-bold text-[#F8FAFC]">{searchUserResult.name}</h4>
                  <p className="text-[11px] font-mono text-[#06B6D4]">@{searchUserResult.username}</p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => sendFriendRequest(searchUserResult.username)}
                  className="px-3 py-1.5 rounded-lg text-xs font-bold bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] text-white flex items-center gap-1 hover:brightness-110 transition-all"
                >
                  <UserPlus className="w-3.5 h-3.5" />
                  <span>طلب صداقة</span>
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Tab switch between Friends & Requests */}
        <div className="flex rounded-xl bg-[#111827] p-1 border border-[#17223B]">
          <button
            onClick={() => setActiveTab('friends')}
            className={`flex-1 py-2 text-xs sm:text-sm font-bold rounded-lg transition-all flex items-center justify-center gap-2 ${
              activeTab === 'friends'
                ? 'bg-[#7C3AED] text-white shadow-sm'
                : 'text-[#94A3B8] hover:text-white'
            }`}
          >
            <span>قائمة الأصدقاء ({friends.length})</span>
          </button>

          <button
            onClick={() => setActiveTab('requests')}
            className={`flex-1 py-2 text-xs sm:text-sm font-bold rounded-lg transition-all flex items-center justify-center gap-2 ${
              activeTab === 'requests'
                ? 'bg-[#7C3AED] text-white shadow-sm'
                : 'text-[#94A3B8] hover:text-white'
            }`}
          >
            <span>طلبات الصداقة</span>
            {friendRequests.length > 0 && (
              <span className="w-5 h-5 rounded-full bg-[#EF4444] text-white text-[11px] font-mono flex items-center justify-center font-bold">
                {friendRequests.length}
              </span>
            )}
          </button>
        </div>

        {/* Tab: Requests */}
        {activeTab === 'requests' && (
          <div className="space-y-3">
            {friendRequests.length === 0 ? (
              <div className="p-8 text-center bg-[#0A0F1D] border border-[#17223B] rounded-2xl">
                <UserCheck className="w-10 h-10 text-[#64748B] mx-auto mb-2" />
                <p className="text-xs text-[#94A3B8]">لا توجد طلبات صداقة معلقة حالياً</p>
              </div>
            ) : (
              friendRequests.map((req) => (
                <div
                  key={req.id}
                  className="p-4 rounded-xl bg-[#0A0F1D] border border-[#17223B] flex items-center justify-between gap-3"
                >
                  <div className="flex items-center gap-3">
                    <CyberAvatar
                      url={req.fromAvatarUrl}
                      name={req.fromName}
                      size="md"
                    />
                    <div>
                      <h4 className="text-xs sm:text-sm font-bold text-[#F8FAFC]">{req.fromName}</h4>
                      <p className="text-[11px] font-mono text-[#06B6D4]">@{req.fromUsername}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => acceptFriendRequest(req)}
                      className="px-3 py-1.5 rounded-lg text-xs font-bold bg-[#10B981] text-white hover:bg-emerald-600 transition-colors flex items-center gap-1"
                    >
                      <Check className="w-3.5 h-3.5" />
                      <span>قبول</span>
                    </button>
                    <button
                      onClick={() => declineFriendRequest(req.id)}
                      className="px-3 py-1.5 rounded-lg text-xs font-bold bg-[#EF4444]/20 border border-[#EF4444]/40 text-[#EF4444] hover:bg-[#EF4444]/30 transition-colors flex items-center gap-1"
                    >
                      <X className="w-3.5 h-3.5" />
                      <span>رفض</span>
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* Tab: Friends */}
        {activeTab === 'friends' && (
          <div className="space-y-3">
            {friends.length === 0 ? (
              <div className="p-8 text-center bg-[#0A0F1D] border border-[#17223B] rounded-2xl">
                <UserPlus className="w-10 h-10 text-[#64748B] mx-auto mb-2" />
                <p className="text-xs text-[#94A3B8]">لم تقم بإضافة أي أصدقاء بعد. استخدم البحث أعلاه!</p>
              </div>
            ) : (
              friends.map((friend) => (
                <div
                  key={friend.userId}
                  className={`p-4 rounded-xl bg-[#0A0F1D] border transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-3 ${
                    friend.isBlocked ? 'border-[#EF4444]/40 opacity-70' : 'border-[#17223B] hover:border-[#7C3AED]/50'
                  }`}
                >
                  <div
                    onClick={() =>
                      viewUserProfile({
                        id: friend.userId,
                        username: friend.username,
                        name: friend.name,
                        avatarUrl: friend.avatarUrl,
                        createdAt: Date.now(),
                      })
                    }
                    className="flex items-center gap-3 cursor-pointer"
                  >
                    <CyberAvatar
                      url={friend.avatarUrl}
                      name={friend.name}
                      size="md"
                      isOnline={friend.isOnline && !friend.isBlocked}
                    />

                    <div>
                      <div className="flex items-center gap-2">
                        <h4 className="text-xs sm:text-sm font-bold text-[#F8FAFC]">{friend.name}</h4>
                        {friend.isBlocked && (
                          <span className="text-[10px] px-1.5 py-0.2 rounded bg-[#EF4444]/20 text-[#EF4444] border border-[#EF4444]/40">
                            محظور
                          </span>
                        )}
                      </div>
                      <p className="text-[11px] font-mono text-[#06B6D4]">@{friend.username}</p>

                      {friend.activeRoomCode && !friend.isBlocked && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            joinRoom(friend.activeRoomCode!);
                          }}
                          className="mt-1 text-[11px] font-mono text-[#10B981] flex items-center gap-1 hover:underline"
                        >
                          <Radio className="w-3 h-3 animate-pulse" />
                          <span>في غرفة: {friend.activeRoomCode} (انقر للدخول)</span>
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Actions: Call, Direct Chat, Block, Remove */}
                  <div className="flex items-center gap-2 shrink-0 pt-2 sm:pt-0 border-t sm:border-t-0 border-[#17223B]">
                    {!friend.isBlocked && (
                      <>
                        <button
                          onClick={() =>
                            startCall({
                              id: friend.userId,
                              username: friend.username,
                              name: friend.name,
                              avatarUrl: friend.avatarUrl,
                              createdAt: Date.now(),
                            })
                          }
                          className="p-2 rounded-xl bg-[#10B981]/15 text-[#10B981] hover:bg-[#10B981]/25 border border-[#10B981]/30 transition-colors"
                          title="مكالمة صوتية مشفرة"
                        >
                          <Phone className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() =>
                            openDirectChat({
                              id: friend.userId,
                              username: friend.username,
                              name: friend.name,
                              avatarUrl: friend.avatarUrl,
                              createdAt: Date.now(),
                            })
                          }
                          className="p-2 rounded-xl bg-[#7C3AED]/15 text-[#8B5CF6] hover:bg-[#7C3AED]/25 border border-[#7C3AED]/30 transition-colors"
                          title="محادثة خاصة"
                        >
                          <MessageSquare className="w-4 h-4" />
                        </button>
                      </>
                    )}

                    <button
                      onClick={() => toggleBlockFriend(friend.userId)}
                      className="p-2 rounded-xl text-[#94A3B8] hover:text-[#EF4444] hover:bg-[#111827] transition-colors"
                      title={friend.isBlocked ? 'إلغاء الحظر' : 'حظر الصديق'}
                    >
                      <ShieldAlert className="w-4 h-4" />
                    </button>

                    <button
                      onClick={() => removeFriend(friend.userId)}
                      className="p-2 rounded-xl text-[#94A3B8] hover:text-[#EF4444] hover:bg-[#111827] transition-colors"
                      title="إزالة من الأصدقاء"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </main>
    </div>
  );
};
