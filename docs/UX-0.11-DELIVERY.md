# Elevare 0.11 — Kullanıcı deneyimi ve değer

## Değişiklikler

- Antrenman: duraklat/başlat, sıradaki hareket ve çıkış sabit alt alanda. Tamamlama kaydı da sabit; teknik açıklama açılır bölümde. Görsel yüksekliği düşürüldü, dekoratif çizgiler kaldırıldı.
- Ana ekran: kompakt marka ve küçük figür; antrenman düğmesiyle birlikte yaşam desteğine yer açıldı. Bölümün amacı ve bu haftanın gerçek seans sayısı açılır bölümde.
- Onboarding: 11 zorunlu soru korunur. Önceki kullanıcı tercihi olan 30 saniyelik sunum, son UX değerlendirmesinin onayıyla 4 saniyeye indirildi. Seçim→sonuç açıklaması ve tekrar eden antrenmanların gün gruplaması eklendi. Uzman incelemesi bekleyen içerikler açıkça belirtilir.
- Rutinler: ana ekranda sıradaki kayıt edilmemiş adımı işaretleme ve geri alma. Atlanan kayıt başarı gibi işaretlenmez. Ayrıntı açıklamaları isteğe bağlı açılır. Antrenman ile günlük destekler ayrıldı.
- Haftalık öneri: yalnızca son 7 günde en az 3 ayrı günde açıkça atlandı olarak kaydedilmiş rutin için saat gözden geçirme veya onaylı ara verme önerilir. Kayıtsız günler başarısızlık sayılmaz, otomatik değişiklik yapılmaz.
- Pro önizlemesi: farklı süre tercihinin mevcut program motoruyla önizlemesi ve açık onayla uygulanması. Açık seans varsa değişiklik engellenir. Başlangıç ve geçmiş korunur. Tüm araçlar bu prototipte açıktır; ödeme ve ücretli hak varmış gibi gösterilmez.
- Hareket rehberi: sade zemin, adım adım metin gezinmesi ve mevcut yavaş/duraklat kontrolleri. Metin adımı animasyonun belirli karesiymiş gibi sunulmaz.
- Ses: çevrimdışı Türkçe ses yoksa ayrıntılı durum ve Android ses ayarlarına erişim. Cihaza bağlı TTS devam eder.
- Profilde yinelenen başlıklar kaldırıldı.

## Bitmiş sayılmayan, dış bağımlılığı olan maddeler

1. Koşu/güç/yoga dozlarının uzman onayı ve yeni uzman programları: yetkili inceleme yok; mevcut güvenlik kapıları kapalı.
2. Profesyonel kayıtlı ses paketi: ses varlığı/lisansı ve gerçek cihaz kabulü gerektirir; hazır değildir.
3. Gerçek Pro satışı: fiyat/teklif kararı, Play Billing, hak doğrulama, iptal ve geri yükleme akışları bu sürüme dahil değildir. Sahte ödeme veya ücretli erişim engeli yoktur.
4. Daha kapsamlı haftalık yeniden planlama: bu sürüm kalıcı süre tercihini açık onayla değiştirir; bütün yaşam koşullarını otomatik optimize eden bir koç değildir.
5. Gerçek kullanıcılarla dönüşüm ve kullanım testi yapılmamıştır; satın alma oranı iddia edilmez.

## Kabul

Kaynak ve birim testleri, Android ekran senaryoları ve ekran görüntüleri ayrı kontrol edilir. Sabit kontrollerin görünürlüğü, 200% yazı boyutunda onboarding, kayıt geri alma, onaysız plan değişmemesi ve mevcut seans geri yükleme korunmalıdır. Emülatör başarısı klinik, fiziksel cihaz veya mağaza onayı değildir.
