import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { CyberAvatar } from '../components/CyberAvatar';
import {
  ArrowRight,
  User,
  Lock,
  Copy,
  Check,
  LogOut,
  Save,
  Shield,
  Sparkles,
} from 'lucide-react';

export const ProfileScreen: React.FC = () => {
  const { currentUser, updateProfile, logout, navigateTo, showToast } = useApp();

  const [name, setName] = useState(currentUser?.name || '');
  const [password, setPassword] = useState('');
  const [avatarUrl, setAvatarUrl] = useState(currentUser?.avatarUrl || '');
  const [copied, setCopied] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const presetAvatars = [
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150&auto=format&fit=crop&q=80',
  ];

  const handleCopyId = () => {
    if (!currentUser) return;
    navigator.clipboard.writeText(currentUser.id);
    setCopied(true);
    showToast(`تم نسخ المعرف: ${currentUser.id}`, 'SUCCESS');
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    await updateProfile(name, password || undefined, avatarUrl || null);
    setIsSaving(false);
  };

  if (!currentUser) return null;

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
          <h2 className="text-base font-bold text-[#F8FAFC]">الملف الشخصي وإعدادات الأمان</h2>
        </div>
      </header>

      <main className="max-w-md mx-auto w-full px-4 pt-6 space-y-6">
        {/* Profile Card Summary */}
        <div className="p-6 rounded-2xl bg-[#0A0F1D] border border-[#17223B] text-center flex flex-col items-center shadow-lg relative overflow-hidden">
          <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-[#7C3AED] via-[#06B6D4] to-[#10B981]" />

          <div className="mb-4">
            <CyberAvatar url={avatarUrl} name={name} size="xl" isOnline={true} />
          </div>

          <h3 className="text-lg font-bold text-[#F8FAFC]">{name}</h3>
          <p className="text-xs font-mono text-[#06B6D4] mt-0.5">@{currentUser.username}</p>

          <div className="mt-3 flex items-center gap-2">
            <button
              onClick={handleCopyId}
              className="text-[11px] font-mono text-[#94A3B8] bg-[#111827] border border-[#17223B] px-3 py-1 rounded-full flex items-center gap-1.5 hover:text-white transition-colors"
            >
              <span>ID: {currentUser.id}</span>
              {copied ? <Check className="w-3 h-3 text-[#10B981]" /> : <Copy className="w-3 h-3" />}
            </button>
          </div>
        </div>

        {/* Edit Form */}
        <form onSubmit={handleSave} className="p-6 rounded-2xl bg-[#0A0F1D] border border-[#17223B] space-y-4 shadow-lg">
          <h4 className="text-sm font-bold text-[#F8FAFC] border-b border-[#17223B] pb-2">
            تعديل البيانات الشخصية
          </h4>

          <div>
            <label className="block text-xs text-[#94A3B8] mb-1">الاسم</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2 text-xs sm:text-sm text-[#F8FAFC] focus:outline-none focus:border-[#7C3AED]"
            />
          </div>

          <div>
            <label className="block text-xs text-[#94A3B8] mb-1">تغيير كلمة المرور (اختياري)</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="اتركها فارغة للإبقاء على الحالية"
              className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2 text-xs sm:text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED]"
            />
          </div>

          <div>
            <label className="block text-xs text-[#94A3B8] mb-1">رابط الصورة الرمزية</label>
            <input
              type="url"
              value={avatarUrl}
              onChange={(e) => setAvatarUrl(e.target.value)}
              placeholder="https://..."
              className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2 text-xs sm:text-sm text-[#F8FAFC] placeholder-[#64748B] font-mono focus:outline-none focus:border-[#7C3AED]"
            />

            <div className="flex items-center gap-2 mt-2">
              <span className="text-[10px] text-[#64748B]">أو اختر صورة جاهزة:</span>
              {presetAvatars.map((url, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setAvatarUrl(url)}
                  className={`w-7 h-7 rounded-full overflow-hidden border transition-all ${
                    avatarUrl === url ? 'border-[#22D3EE] scale-110' : 'border-transparent opacity-70'
                  }`}
                >
                  <img src={url} alt="preset" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          </div>

          <button
            type="submit"
            disabled={isSaving}
            className="w-full mt-4 py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] text-white flex items-center justify-center gap-1.5 shadow-[0_0_15px_rgba(124,58,237,0.3)] hover:brightness-110 active:scale-95 transition-all cursor-pointer"
          >
            <Save className="w-4 h-4" />
            <span>{isSaving ? 'جارٍ الحفظ...' : 'حفظ التعديلات'}</span>
          </button>
        </form>

        {/* Logout button */}
        <button
          onClick={logout}
          className="w-full py-3 rounded-2xl text-xs font-bold bg-[#EF4444]/10 border border-[#EF4444]/30 text-[#EF4444] hover:bg-[#EF4444]/20 transition-colors flex items-center justify-center gap-2 cursor-pointer"
        >
          <LogOut className="w-4 h-4" />
          <span>تسجيل الخروج من الحساب</span>
        </button>
      </main>
    </div>
  );
};
