import React from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from './CyberAvatar';
import {
  X,
  LayoutDashboard,
  Users,
  MessageSquare,
  Radio,
  User,
  ShieldCheck,
  Headphones,
  Download,
  LogOut,
} from 'lucide-react';
import { Screen } from '../types';

interface NavigationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

export const NavigationDrawer: React.FC<NavigationDrawerProps> = ({ isOpen, onClose }) => {
  const {
    currentUser,
    currentScreen,
    navigateTo,
    friendRequests,
    openInAppBrowser,
    downloadUrl,
    supportUrl,
    logout,
  } = useApp();

  if (!isOpen) return null;

  const handleNav = (screen: Screen) => {
    navigateTo(screen);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-40 flex">
      <div
        className="fixed inset-0 bg-black/70 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      <div className="relative w-72 max-w-[80vw] h-full bg-[#0A0F1D] border-e border-[#17223B] shadow-[0_0_30px_rgba(0,0,0,0.8)] flex flex-col justify-between z-10 animate-in slide-in-from-right duration-200">
        <div>
          <div className="p-4 border-b border-[#17223B] flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#7C3AED] to-[#06B6D4] flex items-center justify-center shadow-[0_0_10px_#7C3AED]">
                <Radio className="w-4 h-4 text-white" />
              </div>
              <span className="font-mono font-bold tracking-wider text-base bg-gradient-to-r from-[#8B5CF6] to-[#22D3EE] bg-clip-text text-transparent">
                DARK GROUP
              </span>
            </div>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#111827] transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {currentUser && (
            <div
              onClick={() => handleNav('PROFILE')}
              className="p-4 mx-3 my-3 rounded-xl bg-[#111827] border border-[#17223B] flex items-center gap-3 cursor-pointer hover:border-[#7C3AED]/60 transition-colors"
            >
              <CyberAvatar
                url={currentUser.avatarUrl}
                name={currentUser.name}
                size="md"
                isOnline={true}
              />
              <div className="min-w-0 flex-1">
                <p className="text-sm font-bold text-[#F8FAFC] truncate">{currentUser.name}</p>
                <p className="text-xs font-mono text-[#06B6D4] truncate">@{currentUser.username}</p>
                {currentUser.isAnon && (
                  <span className="inline-block mt-0.5 text-[10px] px-1.5 py-0.2 rounded bg-[#7C3AED]/20 text-[#8B5CF6] border border-[#7C3AED]/40">
                    وضع التخفي 🕶️
                  </span>
                )}
              </div>
            </div>
          )}

          <div className="px-3 py-2 space-y-1 text-sm font-medium">
            <button
              onClick={() => handleNav('DASHBOARD')}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-colors ${
                currentScreen === 'DASHBOARD'
                  ? 'bg-[#7C3AED]/20 text-[#22D3EE] border border-[#7C3AED]/40 font-semibold'
                  : 'text-[#94A3B8] hover:bg-[#111827] hover:text-white'
              }`}
            >
              <div className="flex items-center gap-3">
                <LayoutDashboard className="w-4 h-4 text-[#8B5CF6]" />
                <span>الغرف ولوحة التحكم</span>
              </div>
            </button>

            <button
              onClick={() => handleNav('FRIENDS')}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-colors ${
                currentScreen === 'FRIENDS'
                  ? 'bg-[#7C3AED]/20 text-[#22D3EE] border border-[#7C3AED]/40 font-semibold'
                  : 'text-[#94A3B8] hover:bg-[#111827] hover:text-white'
              }`}
            >
              <div className="flex items-center gap-3">
                <Users className="w-4 h-4 text-[#06B6D4]" />
                <span>أصدقائي</span>
              </div>
              {friendRequests.length > 0 && (
                <span className="px-2 py-0.5 text-[11px] rounded-full bg-[#EF4444] text-white font-mono font-bold animate-pulse">
                  {friendRequests.length}
                </span>
              )}
            </button>

            <button
              onClick={() => handleNav('PRIVATE_CHATS')}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-colors ${
                currentScreen === 'PRIVATE_CHATS'
                  ? 'bg-[#7C3AED]/20 text-[#22D3EE] border border-[#7C3AED]/40 font-semibold'
                  : 'text-[#94A3B8] hover:bg-[#111827] hover:text-white'
              }`}
            >
              <div className="flex items-center gap-3">
                <MessageSquare className="w-4 h-4 text-[#10B981]" />
                <span>المحادثات الخاصة</span>
              </div>
            </button>

            <button
              onClick={() => handleNav('FORUM')}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-colors ${
                currentScreen === 'FORUM'
                  ? 'bg-[#7C3AED]/20 text-[#22D3EE] border border-[#7C3AED]/40 font-semibold'
                  : 'text-[#94A3B8] hover:bg-[#111827] hover:text-white'
              }`}
            >
              <div className="flex items-center gap-3">
                <Radio className="w-4 h-4 text-[#F59E0B]" />
                <span>المنتدى العام</span>
              </div>
            </button>

            <button
              onClick={() => handleNav('PROFILE')}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-colors ${
                currentScreen === 'PROFILE'
                  ? 'bg-[#7C3AED]/20 text-[#22D3EE] border border-[#7C3AED]/40 font-semibold'
                  : 'text-[#94A3B8] hover:bg-[#111827] hover:text-white'
              }`}
            >
              <div className="flex items-center gap-3">
                <User className="w-4 h-4 text-[#8B5CF6]" />
                <span>الملف الشخصي</span>
              </div>
            </button>

            <button
              onClick={() => handleNav('PRIVACY')}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-colors ${
                currentScreen === 'PRIVACY'
                  ? 'bg-[#7C3AED]/20 text-[#22D3EE] border border-[#7C3AED]/40 font-semibold'
                  : 'text-[#94A3B8] hover:bg-[#111827] hover:text-white'
              }`}
            >
              <div className="flex items-center gap-3">
                <ShieldCheck className="w-4 h-4 text-[#22D3EE]" />
                <span>سياسة التشفير والخصوصية</span>
              </div>
            </button>
          </div>
        </div>

        <div className="p-3 border-t border-[#17223B] space-y-1.5 text-xs">
          <button
            onClick={() => {
              openInAppBrowser(supportUrl, 'الدعم الفني ومعلومات المطور');
              onClose();
            }}
            className="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-[#94A3B8] hover:bg-[#111827] hover:text-white transition-colors"
          >
            <Headphones className="w-4 h-4 text-[#06B6D4]" />
            <span>الدعم الفني والمطور</span>
          </button>

          <button
            onClick={() => {
              openInAppBrowser(downloadUrl, 'تحميل التحديثات');
              onClose();
            }}
            className="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-[#94A3B8] hover:bg-[#111827] hover:text-white transition-colors"
          >
            <Download className="w-4 h-4 text-[#10B981]" />
            <span>تحميل التحديثات</span>
          </button>

          <button
            onClick={() => {
              logout();
              onClose();
            }}
            className="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-[#EF4444] hover:bg-[#EF4444]/10 transition-colors font-medium mt-1"
          >
            <LogOut className="w-4 h-4" />
            <span>تسجيل الخروج</span>
          </button>
        </div>
      </div>
    </div>
  );
};
