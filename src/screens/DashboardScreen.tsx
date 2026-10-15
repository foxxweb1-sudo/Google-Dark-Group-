import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { NavigationDrawer } from '../components/NavigationDrawer';
import { CyberAvatar } from '../components/CyberAvatar';
import { RoomInfo } from '../types';
import {
  Menu,
  MessageSquare,
  Users,
  Plus,
  LogIn,
  Lock,
  Unlock,
  Radio,
  Shield,
  KeyRound,
  Sparkles,
  Headphones,
  ExternalLink,
} from 'lucide-react';

export const DashboardScreen: React.FC = () => {
  const {
    currentUser,
    allRooms,
    mySavedRooms,
    friendsRooms,
    friendRequests,
    createRoom,
    joinRoom,
    navigateTo,
    showToast,
  } = useApp();

  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  // Create room state
  const [newRoomName, setNewRoomName] = useState('');
  const [enablePassword, setEnablePassword] = useState(false);
  const [newRoomPassword, setNewRoomPassword] = useState('');
  const [isCreating, setIsCreating] = useState(false);

  // Quick join room state
  const [joinCode, setJoinCode] = useState('');
  const [pendingLockedRoom, setPendingLockedRoom] = useState<RoomInfo | null>(null);
  const [passAttempt, setPassAttempt] = useState('');
  const [showPasswordDialog, setShowPasswordDialog] = useState(false);

  const handleCreateRoom = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newRoomName.trim()) {
      showToast('يرجى كتابة اسم للغرفة', 'ERROR');
      return;
    }
    setIsCreating(true);
    await createRoom(newRoomName, enablePassword ? newRoomPassword : null);
    setIsCreating(false);
    setNewRoomName('');
    setNewRoomPassword('');
    setEnablePassword(false);
  };

  const handleJoinClick = async (room: RoomInfo) => {
    if (room.isLocked && room.hostId !== currentUser?.id) {
      setPendingLockedRoom(room);
      setPassAttempt('');
      setShowPasswordDialog(true);
    } else {
      await joinRoom(room.id);
    }
  };

  const handleDirectJoinByCode = async (e: React.FormEvent) => {
    e.preventDefault();
    const clean = joinCode.trim().toUpperCase();
    if (!clean) return;
    const found = allRooms.find((r) => r.id.toUpperCase() === clean);
    if (found && found.isLocked && found.hostId !== currentUser?.id) {
      setPendingLockedRoom(found);
      setPassAttempt('');
      setShowPasswordDialog(true);
    } else {
      await joinRoom(clean);
    }
  };

  const handleConfirmPasswordJoin = async () => {
    if (!pendingLockedRoom) return;
    const ok = await joinRoom(pendingLockedRoom.id, passAttempt);
    if (ok) {
      setShowPasswordDialog(false);
      setPendingLockedRoom(null);
    }
  };

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC] flex flex-col pb-12">
      {/* Sidebar Navigation Drawer */}
      <NavigationDrawer isOpen={isDrawerOpen} onClose={() => setIsDrawerOpen(false)} />

      {/* Top Application Bar */}
      <header className="sticky top-0 z-30 bg-[#0A0F1D]/90 backdrop-blur-md border-b border-[#17223B] px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => setIsDrawerOpen(true)}
            className="p-2 rounded-xl text-[#94A3B8] hover:text-white hover:bg-[#111827] transition-colors"
            title="القائمة الرئيسية"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#7C3AED] to-[#06B6D4] flex items-center justify-center shadow-[0_0_8px_#7C3AED]">
              <Radio className="w-3.5 h-3.5 text-white" />
            </div>
            <span className="font-mono font-bold tracking-wider text-base bg-gradient-to-r from-[#8B5CF6] to-[#22D3EE] bg-clip-text text-transparent">
              DARK GROUP
            </span>
          </div>
        </div>

        {/* Shortcuts: Private Chats, Friends */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigateTo('FRIENDS')}
            className="relative p-2 rounded-xl text-[#94A3B8] hover:text-[#22D3EE] hover:bg-[#111827] transition-colors"
            title="الأصدقاء"
          >
            <Users className="w-5 h-5" />
            {friendRequests.length > 0 && (
              <span className="absolute top-1 end-1 w-2.5 h-2.5 rounded-full bg-[#EF4444] animate-ping" />
            )}
          </button>

          <button
            onClick={() => navigateTo('PRIVATE_CHATS')}
            className="p-2 rounded-xl text-[#94A3B8] hover:text-[#10B981] hover:bg-[#111827] transition-colors"
            title="الرسائل الخاصة"
          >
            <MessageSquare className="w-5 h-5" />
          </button>

          {currentUser && (
            <div
              onClick={() => navigateTo('PROFILE')}
              className="cursor-pointer ms-1 hover:opacity-80 transition-opacity"
            >
              <CyberAvatar
                url={currentUser.avatarUrl}
                name={currentUser.name}
                size="sm"
                isOnline={true}
              />
            </div>
          )}
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-4xl mx-auto w-full px-4 sm:px-6 pt-6 space-y-6">
        {/* Welcome Greeting Card */}
        <div className="rounded-2xl p-5 bg-gradient-to-r from-[#0A0F1D] via-[#111827] to-[#0A0F1D] border border-[#17223B] flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-[0_0_20px_rgba(0,0,0,0.5)]">
          <div className="flex items-center gap-3.5">
            <CyberAvatar
              url={currentUser?.avatarUrl}
              name={currentUser?.name}
              size="lg"
              isOnline={true}
            />
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-bold text-[#F8FAFC]">{currentUser?.name}</h2>
                {currentUser?.isAnon && (
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#7C3AED]/20 text-[#8B5CF6] border border-[#7C3AED]/40">
                    مجهول
                  </span>
                )}
              </div>
              <p className="text-xs font-mono text-[#06B6D4] mt-0.5">@{currentUser?.username}</p>
              <p className="text-xs text-[#94A3B8] mt-1">مرحباً بك في منظومة الاتصال المشفرة. القنوات مؤمنة ومحدثة.</p>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={() => navigateTo('FORUM')}
              className="px-3.5 py-2 rounded-xl text-xs font-bold bg-[#111827] border border-[#17223B] hover:border-[#F59E0B] text-[#F8FAFC] hover:text-[#F59E0B] transition-all flex items-center gap-1.5"
            >
              <Radio className="w-3.5 h-3.5 text-[#F59E0B]" />
              <span>المنتدى العام</span>
            </button>
          </div>
        </div>

        {/* Room Action Grid: Create Room & Quick Join */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Create Room Card */}
          <div className="rounded-2xl p-5 bg-[#0A0F1D] border border-[#17223B] flex flex-col justify-between shadow-lg">
            <div>
              <div className="flex items-center gap-2.5 mb-3">
                <div className="w-8 h-8 rounded-lg bg-[#7C3AED]/20 text-[#8B5CF6] flex items-center justify-center">
                  <Plus className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[#F8FAFC]">إنشاء غرفة مشفرة جديدة</h3>
                  <p className="text-[11px] text-[#94A3B8]">قناة صوتية فورية مع تشفير المحادثات</p>
                </div>
              </div>

              <form onSubmit={handleCreateRoom} className="space-y-3 mt-4">
                <div>
                  <label className="block text-xs text-[#94A3B8] mb-1">اسم الغرفة</label>
                  <input
                    type="text"
                    required
                    value={newRoomName}
                    onChange={(e) => setNewRoomName(e.target.value)}
                    placeholder="مثال: غرفة المطورين #01"
                    className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-3.5 py-2 text-xs text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED]"
                  />
                </div>

                <div className="flex items-center justify-between p-2.5 rounded-xl bg-[#111827]/70 border border-[#17223B]">
                  <div className="flex items-center gap-2 text-xs">
                    <Lock className={`w-3.5 h-3.5 ${enablePassword ? 'text-[#8B5CF6]' : 'text-[#64748B]'}`} />
                    <span className="text-[#F8FAFC]">قفل الغرفة بكلمة مرور</span>
                  </div>
                  <input
                    type="checkbox"
                    checked={enablePassword}
                    onChange={(e) => setEnablePassword(e.target.checked)}
                    className="w-4 h-4 accent-[#7C3AED] cursor-pointer"
                  />
                </div>

                {enablePassword && (
                  <div>
                    <label className="block text-xs text-[#94A3B8] mb-1">كلمة مرور الغرفة</label>
                    <input
                      type="password"
                      required={enablePassword}
                      value={newRoomPassword}
                      onChange={(e) => setNewRoomPassword(e.target.value)}
                      placeholder="••••••"
                      className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-3.5 py-2 text-xs text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED]"
                    />
                  </div>
                )}

                <button
                  type="submit"
                  disabled={isCreating}
                  className="w-full py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-[#7C3AED] to-[#8B5CF6] text-white shadow-[0_0_15px_rgba(124,58,237,0.3)] hover:brightness-110 active:scale-95 transition-all flex items-center justify-center gap-1.5 cursor-pointer"
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>{isCreating ? 'جارٍ إنشاء الغرفة...' : 'إنشاء ودخول الغرفة'}</span>
                </button>
              </form>
            </div>
          </div>

          {/* Quick Join Card */}
          <div className="rounded-2xl p-5 bg-[#0A0F1D] border border-[#17223B] flex flex-col justify-between shadow-lg">
            <div>
              <div className="flex items-center gap-2.5 mb-3">
                <div className="w-8 h-8 rounded-lg bg-[#06B6D4]/20 text-[#22D3EE] flex items-center justify-center">
                  <LogIn className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[#F8FAFC]">انضمام مباشر بكود الغرفة</h3>
                  <p className="text-[11px] text-[#94A3B8]">ادخل كود الغرفة المشفر للانضمام فوراً</p>
                </div>
              </div>

              <form onSubmit={handleDirectJoinByCode} className="space-y-3 mt-4">
                <div>
                  <label className="block text-xs text-[#94A3B8] mb-1">كود الغرفة</label>
                  <input
                    type="text"
                    required
                    value={joinCode}
                    onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
                    placeholder="مثال: ROOM-8821 أو NODE-102"
                    className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-3.5 py-2 text-xs text-[#F8FAFC] font-mono placeholder-[#64748B] focus:outline-none focus:border-[#06B6D4]"
                  />
                </div>

                <div className="p-3 rounded-xl bg-[#111827]/40 border border-[#17223B] text-[11px] text-[#94A3B8] leading-relaxed">
                  الغرف المشفرة تدعم التحدث بالصوت عالي النقاء والكتم الفوري وإرسال الرسائل الحية المشفرة.
                </div>

                <button
                  type="submit"
                  className="w-full py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-[#06B6D4] to-[#22D3EE] text-[#040711] shadow-[0_0_15px_rgba(6,182,212,0.3)] hover:brightness-110 active:scale-95 transition-all flex items-center justify-center gap-1.5 cursor-pointer font-bold"
                >
                  <LogIn className="w-3.5 h-3.5" />
                  <span>انضمام للغرفة الآن</span>
                </button>
              </form>
            </div>
          </div>
        </div>

        {/* My Saved / Created Rooms */}
        {mySavedRooms.length > 0 && (
          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-bold text-[#F8FAFC] flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-[#8B5CF6]" />
                <span>غرفتي التي أنشأتها ({mySavedRooms.length})</span>
              </h3>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
              {mySavedRooms.map((room) => (
                <div
                  key={room.id}
                  className="p-4 rounded-xl bg-[#0A0F1D] border border-[#17223B] hover:border-[#7C3AED]/70 transition-all flex flex-col justify-between gap-3 group"
                >
                  <div>
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-[11px] font-mono text-[#06B6D4] bg-[#111827] px-2 py-0.5 rounded border border-[#17223B]">
                        {room.id}
                      </span>
                      {room.isLocked ? (
                        <Lock className="w-3.5 h-3.5 text-[#EF4444]" />
                      ) : (
                        <Unlock className="w-3.5 h-3.5 text-[#10B981]" />
                      )}
                    </div>
                    <h4 className="text-sm font-bold text-[#F8FAFC] mt-2 group-hover:text-[#22D3EE] transition-colors truncate">
                      {room.name}
                    </h4>
                    <p className="text-[11px] text-[#94A3B8] mt-1">المنشئ: أنت</p>
                  </div>

                  <button
                    onClick={() => handleJoinClick(room)}
                    className="w-full py-1.5 rounded-lg text-xs font-bold bg-[#111827] hover:bg-[#7C3AED] hover:text-white text-[#94A3B8] border border-[#17223B] transition-colors"
                  >
                    دخول الغرفة
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Active Public / Friends' Encrypted Rooms */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-bold text-[#F8FAFC] flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-[#10B981] animate-ping" />
              <span>الغرف النشطة على الشبكة ({friendsRooms.length})</span>
            </h3>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
            {friendsRooms.map((room) => (
              <div
                key={room.id}
                className="p-4 rounded-xl bg-[#0A0F1D] border border-[#17223B] hover:border-[#06B6D4]/70 transition-all flex flex-col justify-between gap-3 group"
              >
                <div>
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-[11px] font-mono text-[#22D3EE] bg-[#111827] px-2 py-0.5 rounded border border-[#17223B]">
                      {room.id}
                    </span>
                    {room.isLocked ? (
                      <span className="flex items-center gap-1 text-[10px] text-[#EF4444] bg-[#EF4444]/10 px-2 py-0.5 rounded">
                        <Lock className="w-3 h-3" />
                        <span>محمية</span>
                      </span>
                    ) : (
                      <span className="flex items-center gap-1 text-[10px] text-[#10B981] bg-[#10B981]/10 px-2 py-0.5 rounded">
                        <Unlock className="w-3 h-3" />
                        <span>مفتوحة</span>
                      </span>
                    )}
                  </div>
                  <h4 className="text-sm font-bold text-[#F8FAFC] mt-2 group-hover:text-[#22D3EE] transition-colors truncate">
                    {room.name}
                  </h4>
                  <p className="text-[11px] text-[#94A3B8] mt-1">المنشئ: {room.host}</p>
                </div>

                <button
                  onClick={() => handleJoinClick(room)}
                  className="w-full py-1.5 rounded-lg text-xs font-bold bg-[#111827] hover:bg-[#06B6D4] hover:text-[#040711] text-[#22D3EE] border border-[#17223B] transition-colors"
                >
                  انضمام للغرفة
                </button>
              </div>
            ))}
          </div>
        </div>
      </main>

      {/* Password Prompt Modal for Locked Room */}
      {showPasswordDialog && pendingLockedRoom && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#0A0F1D] border border-[#17223B] p-6 shadow-2xl">
            <div className="flex items-center gap-2 mb-3 text-[#EF4444]">
              <Lock className="w-5 h-5" />
              <h3 className="text-sm font-bold text-[#F8FAFC]">الغرفة مشفرة بكلمة مرور</h3>
            </div>
            <p className="text-xs text-[#94A3B8] mb-4">
              الغرفة <span className="text-[#22D3EE] font-mono">[{pendingLockedRoom.id}]</span> تتطلب كلمة مرور للدخول:
            </p>

            <input
              type="password"
              autoFocus
              value={passAttempt}
              onChange={(e) => setPassAttempt(e.target.value)}
              placeholder="أدخل كلمة المرور"
              className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED] mb-4"
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleConfirmPasswordJoin();
              }}
            />

            <div className="flex gap-2">
              <button
                onClick={() => {
                  setShowPasswordDialog(false);
                  setPendingLockedRoom(null);
                }}
                className="flex-1 py-2 rounded-xl text-xs font-bold bg-[#111827] text-[#94A3B8] hover:text-white transition-colors"
              >
                إلغاء
              </button>
              <button
                onClick={handleConfirmPasswordJoin}
                className="flex-1 py-2 rounded-xl text-xs font-bold bg-[#7C3AED] hover:bg-[#8B5CF6] text-white transition-colors"
              >
                تأكيد الدخول
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
