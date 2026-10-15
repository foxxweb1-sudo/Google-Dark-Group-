import React from 'react';
import { useApp } from '../context/AppContext';
import { ArrowRight, ShieldCheck, Lock, EyeOff, Radio, Server } from 'lucide-react';

export const PrivacyPolicyScreen: React.FC = () => {
  const { navigateTo, currentUser } = useApp();

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC] flex flex-col pb-12">
      {/* Top Header */}
      <header className="sticky top-0 z-30 bg-[#0A0F1D]/90 backdrop-blur-md border-b border-[#17223B] px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigateTo(currentUser ? 'DASHBOARD' : 'LANDING')}
            className="p-1.5 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#111827] transition-colors"
          >
            <ArrowRight className="w-5 h-5" />
          </button>
          <h2 className="text-base font-bold text-[#F8FAFC]">سياسة الخصوصية وأمان الشبكة</h2>
        </div>
      </header>

      <main className="max-w-2xl mx-auto w-full px-4 pt-6 space-y-5">
        <div className="p-5 rounded-2xl bg-[#0A0F1D] border border-[#17223B] space-y-4">
          <div className="flex items-center gap-3 text-[#10B981]">
            <ShieldCheck className="w-6 h-6" />
            <h3 className="text-base font-bold text-[#F8FAFC]">تشفير البيانات والاتصال الصوتي</h3>
          </div>
          <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
            تعتمد منظومة Dark Group تقنيات تشفير حزم الصوت والرسائل النصية من النهاية إلى النهاية (End-to-End Encryption). لا يتم تسجيل أو تخزين أي حزم صوتية أو مكالمات خاصة في أي قواعد بيانات خارجية أو سحابية.
          </p>
        </div>

        <div className="p-5 rounded-2xl bg-[#0A0F1D] border border-[#17223B] space-y-4">
          <div className="flex items-center gap-3 text-[#22D3EE]">
            <EyeOff className="w-6 h-6" />
            <h3 className="text-base font-bold text-[#F8FAFC]">وضع التخفي وعدم جمع الهوية</h3>
          </div>
          <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
            يوفر النظام خيار التخفي (Anon Mode) الذي يمنع ربط عنوان IP أو أي بيانات وصفية بحسابك داخل الغرف الصوتية العامة، مع استخدام معرفات عشوائية مؤقتة للجلسات.
          </p>
        </div>

        <div className="p-5 rounded-2xl bg-[#0A0F1D] border border-[#17223B] space-y-4">
          <div className="flex items-center gap-3 text-[#8B5CF6]">
            <Server className="w-6 h-6" />
            <h3 className="text-base font-bold text-[#F8FAFC]">حذف الرسائل والمحتوى</h3>
          </div>
          <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
            يحق لكل مستخدم في المحادثات الخاصة المشفرة حذف أي رسالة أو تسجيل صوتي لديه فقط أو لدى الطرف الآخر في أي وقت وبشكل فوري ونهائي.
          </p>
        </div>

        <div className="p-5 rounded-2xl bg-[#0A0F1D] border border-[#17223B] space-y-4">
          <div className="flex items-center gap-3 text-[#F59E0B]">
            <Radio className="w-6 h-6" />
            <h3 className="text-base font-bold text-[#F8FAFC]">غرف المحادثة المغلقة</h3>
          </div>
          <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
            الغرف المحمية بكلمة مرور لا يمكن لأي طرف ثالث أو حتى مديري الخوادم الوصول إلى مجريات النقاش الصوتي داخلها دون إدخال مفتاح التشفير وكلمة المرور الخاصة بالمؤسس.
          </p>
        </div>
      </main>
    </div>
  );
};
