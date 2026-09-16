# Astra Mobile

Astra هو تحويل MobileClaw إلى مساعد هاتف ذكي قابل للتوسعة. المرحلة الحالية تضيف طبقة AI Router مستقلة تدعم عدة مزودين مع fallback تلقائي.

## المزودون

- Google Gemini
- Groq
- OpenRouter
- Hugging Face

## السلوك

1. يستخدم Astra أول مزود متاح.
2. عند timeout أو 429 أو أخطاء الخادم ينتقل تلقائياً إلى المزود التالي.
3. لا توجد مفاتيح API داخل المستودع.
4. يمكن تمرير المفاتيح من طبقة الإعدادات الآمنة في التطبيق.
5. كل استجابة تسجل المزود المستخدم ووقت الاستجابة وحالة fallback.

## بنية المشروع

```text
Astra UI
  -> AgentRuntime
      -> AIRouter
          -> Gemini
          -> Groq
          -> OpenRouter
          -> Hugging Face
      -> Android Tools
      -> Memory / Tasks
```

## ملاحظة

Jina AI يستخدم لاحقاً للبحث والـembeddings وإعادة الترتيب، وليس كمزود chat عام داخل الـfallback router.

## بناء APK

GitHub Actions يبني نسخة Debug تلقائياً عند كل push إلى `main`، ويضع APK كـartifact باسم `debug-apk`.
