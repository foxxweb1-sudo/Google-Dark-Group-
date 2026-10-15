import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Radio, User, AtSign, Eye, EyeOff, Ghost, ShieldCheck } from 'lucide-react';

export const LandingScreen: React.FC = () => {
  const { register, login, navigateTo, openInAppBrowser, supportUrl } = useApp();
  const [tab, setTab] = useState<'login' | 'register'>('register');

  const [name, setName] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isAnon, setIsAnon] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const presetAvatars = [
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150&auto=format&fit=crop&q=80',
  ];

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    await register(name, username, password, isAnon, avatarUrl || null);
    setLoading(false);
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    await login(username, password);
    setLoading(false);
  };

  const quickDemoLogin = async (demoUser: string, demoPass: string) => {
    setLoading(true);
    await login(demoUser, demoPass);
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC] flex flex-col justify-between p-4 sm:p-6 relative overflow-hidden">
      <div className="absolute top-0 right-1/4 w-96 h-96 bg-[#7C3AED]/20 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-0 left-1/4 w-96 h-96 bg-[#06B6D4]/15 rounded-full blur-[120px] pointer-events-none" />

      <div className="flex items-center justify-between max-w-xl mx-auto w-full pt-2">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#7C3AED] to-[#06B6D4] flex items-center justify-center shadow-[0_0_15px_#7C3AED]">
            <Radio className="w-5 h-5 text-white" />
          </div>
          <span className="font-mono font-bold tracking-wider text-lg bg-gradient-to-r from-[#8B5CF6] to-[#22D3EE] bg-clip-text text-transparent">
            DARK GROUP
          </span>
        </div>
        <button
          onClick={() => navigateTo('PRIVACY')}
          className="text-xs text-[#94A3B8] hover:text-[#22D3EE] transition-colors flex items-center gap-1"
        >
          <ShieldCheck className="w-3.5 h-3.5 text-[#10B981]" />
          <span>بروتوكول التشفير</span>
        </button>
      </div>

      <div className="max-w-md mx-auto w-full my-6 bg-[#0A0F1D]/90 border border-[#17223B] rounded-2xl p-6 sm:p-8 backdrop-blur-xl shadow-[0_0_35px_rgba(0,0,0,0.6)]">
        <div className="flex rounded-xl bg-[#111827] p-1 border border-[#17223B] mb-6">
          <button
            type="button"
            onClick={() => setTab('register')}
            className={`flex-1 py-2 text-sm font-bold rounded-lg transition-all ${
              tab === 'register'
                ? 'bg-gradient-to-r from-[#7C3AED] to-[#8B5CF6] text-white shadow-[0_0_12px_rgba(124,58,237,0.4)]'
                : 'text-[#94A3B8] hover:text-white'
            }`}
          >
            حساب جديد
          </button>
          <button
            type="button"
            onClick={() => setTab('login')}
            className={`flex-1 py-2 text-sm font-bold rounded-lg transition-all ${
              tab === 'login'
                ? 'bg-gradient-to-r from-[#7C3AED] to-[#8B5CF6] text-white shadow-[0_0_12px_rgba(124,58,237,0.4)]'
                : 'text-[#94A3B8] hover:text-white'
            }`}
          >
            تسجيل الدخول
          </button>
        </div>

        {tab === 'register' ? (
          <form onSubmit={handleRegister} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-[#94A3B8] mb-1.5">الاسم المستعار أو الحقيقي</label>
              <div className="relative">
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="مثال: المستكشف السيبراني"
                  className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED] transition-colors"
                />
                <User className="absolute end-3 top-3 w-4 h-4 text-[#64748B]" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-[#94A3B8] mb-1.5">اسم المستخدم (بالإنجليزي فقط)</label>
              <div className="relative">
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value.toLowerCase().replace(/[^a-z0-9_]/g, ''))}
                  placeholder="username_123"
                  className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-sm text-[#F8FAFC] placeholder-[#64748B] font-mono focus:outline-none focus:border-[#7C3AED] transition-colors"
                />
                <AtSign className="absolute end-3 top-3 w-4 h-4 text-[#64748B]" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-[#94A3B8] mb-1.5">كلمة المرور المشفرة</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED] transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute end-3 top-3 text-[#64748B] hover:text-white"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-[#111827]/60 border border-[#17223B]">
              <div className="flex items-center gap-2">
                <Ghost className={`w-4 h-4 ${isAnon ? 'text-[#8B5CF6]' : 'text-[#64748B]'}`} />
                <div>
                  <p className="text-xs font-bold text-[#F8FAFC]">وضع التخفي الكامل (Anon)</p>
                  <p className="text-[10px] text-[#94A3B8]">إخفاء البصمة الرقمية والهوية في الغرف</p>
                </div>
              </div>
              <input
                type="checkbox"
                checked={isAnon}
                onChange={(e) => setIsAnon(e.target.checked)}
                className="w-4 h-4 accent-[#7C3AED] cursor-pointer"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-[#94A3B8] mb-2">اختر صورة أو صورة افتراضية</label>
              <div className="flex items-center gap-2">
                {presetAvatars.map((url, idx) => (
                  <button
                    key={idx}
                    type="button"
                    onClick={() => setAvatarUrl(url)}
                    className={`w-9 h-9 rounded-full overflow-hidden border-2 transition-all ${
                      avatarUrl === url ? 'border-[#22D3EE] scale-110 shadow-[0_0_10px_#22D3EE]' : 'border-transparent opacity-70 hover:opacity-100'
                    }`}
                  >
                    <img src={url} alt="preset" className="w-full h-full object-cover" />
                  </button>
                ))}
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-4 py-3 rounded-xl font-bold bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] text-white shadow-[0_0_20px_rgba(124,58,237,0.35)] hover:brightness-110 active:scale-95 transition-all flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
            >
              {loading ? 'جارٍ إنشاء الحساب المشفر...' : 'إنشاء حساب في Dark Group'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-[#94A3B8] mb-1.5">اسم المستخدم</label>
              <div className="relative">
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value.toLowerCase().replace(/[^a-z0-9_]/g, ''))}
                  placeholder="username_123"
                  className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-sm text-[#F8FAFC] placeholder-[#64748B] font-mono focus:outline-none focus:border-[#7C3AED] transition-colors"
                />
                <AtSign className="absolute end-3 top-3 w-4 h-4 text-[#64748B]" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-[#94A3B8] mb-1.5">كلمة المرور</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full bg-[#111827] border border-[#17223B] rounded-xl px-4 py-2.5 text-sm text-[#F8FAFC] placeholder-[#64748B] focus:outline-none focus:border-[#7C3AED] transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute end-3 top-3 text-[#64748B] hover:text-white"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-4 py-3 rounded-xl font-bold bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] text-white shadow-[0_0_20px_rgba(124,58,237,0.35)] hover:brightness-110 active:scale-95 transition-all flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
            >
              {loading ? 'جارٍ التحقق...' : 'تسجيل الدخول الآن'}
            </button>
          </form>
        )}

        <div className="mt-6 pt-5 border-t border-[#17223B]">
          <p className="text-[11px] text-[#64748B] text-center mb-2.5">أو الدخول بحساب تجريبي فوري:</p>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => quickDemoLogin('foxx_root', '')}
              className="flex-1 py-1.5 px-2 rounded-lg bg-[#111827] border border-[#17223B] hover:border-[#7C3AED] text-xs font-mono text-[#22D3EE] transition-colors truncate"
            >
              المطور: @foxx_root
            </button>
            <button
              type="button"
              onClick={() => quickDemoLogin('cipher_sec', '')}
              className="flex-1 py-1.5 px-2 rounded-lg bg-[#111827] border border-[#17223B] hover:border-[#06B6D4] text-xs font-mono text-[#10B981] transition-colors truncate"
            >
              المستخدم: @cipher_sec
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-md mx-auto w-full flex items-center justify-center gap-6 text-xs text-[#64748B] pb-2">
        <button
          onClick={() => openInAppBrowser(supportUrl, 'معلومات المطور والدعم')}
          className="hover:text-[#94A3B8] transition-colors"
        >
          الدعم الفني
        </button>
        <span>•</span>
        <button
          onClick={() => navigateTo('PRIVACY')}
          className="hover:text-[#94A3B8] transition-colors"
        >
          سياسة الخصوصية
        </button>
      </div>
    </div>
  );
};
