# Elevare 0.10 — Training Arc / Yaşam ritmi

## Uygulanan kapsam

Antrenman ana eylem olarak korunur. Üç ana bölüm: Bugün, Rutinim, Rehber. Profil üst sağdadır. Kömür, sıcak mercan ve kırık beyaz Training Arc paleti, özgün çizgisel ikonlar ve mevcut çizgi adam hareket geometrisi korunur. Yeni fotoğraf, gerçekçi insan veya ağır görsel bağımlılık eklenmez.

- Kullanıcı seçmeden destek rutini atanmaz. İlk seçim en fazla ikidir; sadece antrenman seçeneği vardır.
- Sabah hazırlığı, akşam kapanışı, su molası, öğün hazırlığı, mevcut kişisel bakım ve odak hazırlığı olmak üzere altı isteğe bağlı şablon bulunur.
- Bugün ekranında en fazla iki destek rutini görünür. Ayrıntılar Rutinim'e taşınır.
- Her rutin için günler, saat, aç/kapat ve ayrı bildirim tercihi vardır. Bildirimler başlangıçta kapalıdır.
- Yapıldı ve atlandı birbirinden ayrılır. İşaretleme geri alınabilir. Rutini silmek açık onay ister ve yalnız o rutinin kayıtlarını siler.
- Son yedi günün yapılan adım sayısı ve iki soruluk haftalık değerlendirme vardır. Sağlık skoru, cezalı seri veya otomatik yük artışı yoktur.
- Uyku, antrenman ve yeni rutinler ortak günlük 1/2 bildirim bütçesi ve en az üç saat aralık kullanır. Uyku, aktif seans, isteğe bağlı okul/iş sessizliği ve gecikme kontrolleri teslimatta yeniden değerlendirilir.
- Hatırlatmalar kesin saatli tıbbi alarm değildir. Gecikmiş bildirimler için telafi kuyruğu yoktur. Rutin revizyonu eski plan alarmını geçersiz kılar.
- Yerel kayıt şeması 9'dur. Eski kayıtlar boş yaşam katmanıyla açılır; zorunlu onboarding tekrarı yoktur. JSON dışa aktarımı yaşam kayıtlarını içerir.

## Korunan yapı

11 soruluk zorunlu onboarding, 30 saniyelik hazırlık, tek 3 günlük demo girişi, 90 günlük program, seans geri yükleme, sesli koç ve mevcut uzman onay kapıları korunur. Antrenman planını duraklatmak bağımsız destek rutinlerini otomatik kapatmaz; bu ayrım ekranda belirtilir.

## Bilinçli olarak eklenmeyenler

Vitamin/takviye dozları, reçete/ilaç takibi, hormon ölçümü veya artış iddiası, boy garantisi, görünüş puanı, kalori/kilo baskısı, bulut hesabı, sensörler, Health Connect, gerçek abonelik ve ödeme yoktur. Odak modülü zamanlayıcı değil, iki adımlık hazırlıktır. Bakım modülü ürün veya tedavi önermez. Haftalık değerlendirme seçimleri programı gizlice değiştirmez.

## Doğrulama ve yayın sınırı

Saf kural testleri, kayıt serileştirme, kaynak derlemesi ve GitHub Actions Android build/emülatör kontrolleri ayrı kanıtlardır. Başarılı derleme, fiziksel cihaz doğrulaması, uzman hareket onayı veya Google Play yayın onayı anlamına gelmez. Uzman onay kapıları kapalı kalır. Üretici pil kısıtlamaları nedeniyle gerçek cihaz bildirim kontrolleri ayrıca gerekir. API 33+ bildirim izin ekranı, erişilebilirlik ve farklı ekran boyutları ek kabul kapılarıdır.

## Kabul senaryoları

1. Eski kullanıcı verisi açıldığında seanslar ve program korunur, rutin atanmaz.
2. İlk iki rutin seçilir, bildirim kendiliğinden açılmaz; ev ekranı iki satırı aşmaz.
3. Tamamla, atla, geri al, uygulamayı yeniden aç işlemleri aynı kaydı üretir.
4. Bir rutin silinince diğer rutin, antrenman ve uyku kayıtları kalır.
5. Gün/saat değiştirilince eski revizyon alarmı reddedilir; sessiz saatte gönderim olmaz.
6. Bir günde bütçe veya üç saat aralık aşılmaz; tarih değişimi aralık kuralını kaldırmaz.
7. Mevcut onboarding ve seans duraklatma/geri yükleme akışları çalışır.
