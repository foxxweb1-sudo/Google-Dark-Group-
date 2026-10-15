import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Mic, Bell, Shield, ArrowLeft, CheckCircle2 } from 'lucide-react';

export const PermissionsScreen: React.FC = () => {
  const { navigateTo, currentUser, showToast } = useApp();
  const [micGranted, setMicGranted] = useState(false);
  const [notifGranted, setNotifGranted] = useState(false);

  const requestMic = async () => {
    try {
      if (navigator.mediaDevices?.getUserMedia) {
        await navigator.mediaDevices.getUserMedia({ audio: true });
        setMicGranted(true);
        showToast('تم منح إذن الميكروفون بنجاح!', 'SUCCESS');
      } else {
        setMicGranted(true);
      }
    } catch {
      showToast('لم يتم منح إذن الميكروفون، يمكنك استخدام وضع المحاكاة.', 'INFO');
      setMicGranted(true);
    }
  };

  const requestNotif = async () => {
    try {
      if ('Notification' in window) {
        const res = await Notification.requestPermission();
        if (res === 'granted') {
          setNotifGranted(true);
          showToast('تم تفعيل إشعارات المكالمات والرسائل!', 'SUCCESS');
        } else {
          setNotifGranted(true);
        }
      } else {
        setNotifGranted(true);
      }
    } catch {
      setNotifGranted(true);
    }
  };

  const handleFinish = () => {
    if (currentUser) {
      navigateTo('DASHBOARD');
    } else {
      navigateTo('LANDING');
    }
  };

  return (
    <div className="min-h-screen bg-[#040711] flex flex-col justify-between p-6 max-w-lg mx-auto">
      <div>
        <div className="flex items-center gap-2 mb-8">
          <div className="w-10 h-10 rounded-xl bg-[#7C3AED]/20 border border-[#7C3AED]/50 flex items-center justify-center">
            <Shield className="w-5 h-5 text-[#22D3EE]" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-[#F8FAFC]">أذونات النظام المشفر</h1>
            <p className="text-xs text-[#94A3B8]">مطلوبة لضمان تجربة الاتصال الصوتي واستقبال الرنين</p>
          </div>
        </div>

        <div className="space-y-4">
          <div className="p-4 rounded-xl bg-[#0A0F1D] border border-[#17223B] flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-full bg-[#111827] flex items-center justify-center text-[#7C3AED]">
                <Mic className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[#F8FAFC]">إذن الميكروفون</h3>
                <p className="text-xs text-[#94A3B8]">للبث الصوتي في الغرف والمكالمات الفورية</p>
              </div>
            </div>
            <button
              onClick={requestMic}
              disabled={micGranted}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                micGranted
                  ? 'bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/40'
                  : 'bg-[#7C3AED] hover:bg-[#8B5CF6] text-white'
              }`}
            >
              {micGranted ? <CheckCircle2 className="w-4 h-4 inline me-1" /> : null}
              {micGranted ? 'ممنوح' : 'سماح'}
            </button>
          </div>

          <div className="p-4 rounded-xl bg-[#0A0F1D] border border-[#17223B] flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-full bg-[#111827] flex items-center justify-center text-[#06B6D4]">
                <Bell className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[#F8FAFC]">إذن الإشعارات</h3>
                <p className="text-xs text-[#94A3B8]">للتنبيه بالمكالمات الواردة والرسائل الخاصة</p>
              </div>
            </div>
            <button
              onClick={requestNotif}
              disabled={notifGranted}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                notifGranted
                  ? 'bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/40'
                  : 'bg-[#06B6D4] hover:bg-[#22D3EE] text-[#040711]'
              }`}
            >
              {notifGranted ? <CheckCircle2 className="w-4 h-4 inline me-1" /> : null}
              {notifGranted ? 'ممنوح' : 'سماح'}
            </button>
          </div>
        </div>
      </div>

      <div className="mt-8">
        <button
          onClick={handleFinish}
          className="w-full py-3 rounded-xl font-bold bg-gradient-to-r from-[#7C3AED] to-[#06B6D4] text-white flex items-center justify-center gap-2 hover:brightness-110 active:scale-95 transition-all shadow-[0_0_20px_rgba(124,58,237,0.3)]"
        >
          <span>متابعة</span>
          <ArrowLeft className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
