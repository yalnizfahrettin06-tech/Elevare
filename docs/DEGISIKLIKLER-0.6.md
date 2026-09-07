# Elevare 0.6 — Seçilen iyileştirmeler
Bu sürüm 0.5 inceleme raporunun önceki kullanıcı kararlarıyla uyumlu önerilerini uygular. APK üretilmedi.

## Önceki kararlarla çeliştiği için uygulanmayanlar
- Zorunlu sorular opsiyonel yapılmadı. Yaş, öncelik, büyüme gözlemi, uyku, hareket sıklığı, süre ve uygunluk yanıtları korunuyor.
- 45 saniye 4–6 saniyeye indirilmedi.
- 3 günlük deneme ilk antrenman sonrasına taşınmadı; onboarding sonunda.
- Yaşlar 13–21, süreler 5/10/15/30 dakika olarak korundu.
- İlk karşılama, 90 günlük takvim ve dört sekme kaldırılmadı. Bu turda kapsamlı yapı değişikliğinin yerine öncelikli akışlar iyileştirildi.
- Gerçekçi insan, yüz, kıyafet ve kas çizimi eklenmedi.

## Figürler
Sprint için ayrı yandan çizim rehberi eklendi: dört ana poz, aralarında döngü, karşı kol/bacak vurgusu ve ilerleme oku. Kol ve bacaklar açısal olarak döndürülür; çizgi uzunlukları döngü boyunca sabittir.
Rehbere Antrenman sekmesindeki “Sprint çizim rehberi” satırından ulaşılır.
Bu bir görsel inceleme ekranıdır; süreli sprint egzersizi veya maksimal sprint programı eklenmedi. Koşuya hazırlık seansı mevcut başlangıç hareketleriyle devam eder. Kullanıcının istediği sprint gösterimi artık ayrı bir rehberde vardır; bu ayrım ürün ekranında da belirtilir.

Diğer hareketler için başlangıçta kısa bekleme, hareket, bitişte kısa bekleme ve geri dönüş fazları eklendi. Hareket türüne göre süre değişir: otur-kalk ve duvar şınavı daha yavaş gösterilir.
Duvar, sandalye ve destek çizgileri belirginleştirildi. İlgili uzuvlar mavi vurguyla çizilir. Yukarı uzanma ve topuk yükseltmede yön oku vardır.
Küçük kartlardaki sabit figür hareketin belirgin pozunu gösterir. Seans süresi bitince animasyon durur. Azaltılmış hareket tercihi korunur.
Sprint dışındaki eski figürler konumlar arasında geçiş kullanır; onların tamamına sabit uzuv uzunluğu sistemi uygulanmadı.
Animasyonlar teknik açıklamaya eşlik eden şemalardır. Fiziksel teknik doğruluğu veya cihazdaki akıcılığı uzman/cihaz testiyle henüz doğrulanmadı.

## Onboarding
Yedi zorunlu soru korunur. 45 saniyelik hazırlık sırasında önce öncelik ve zaman, ardından öneri nedeni, sonra ilk seans görünür. Son ekranda tekrar eden metin azaltıldı.
Hazırlıktan geri dönüp cevapları değiştirmeden ilerlemek beklemeyi baştan başlatmaz. Yanıt değişikliği hazırlığı sıfırlar.
Sistem geri tuşu ve ekrandaki geri düğmesi deneme ekranında aynı hedefe döner.
Profil düzenlerken kayıtlı günlük süre yeniden seçilmeye zorlanmaz. Yeni kurulumda süre yanıtı hâlâ zorunludur.
Bu ekran basit rutin eşleştirmesi yapar; hormon düzeyi, kemik yaşı veya büyüme yüzdesi hesaplamaz.

## Ana ekran
Antrenman tamamlandıktan sonra bugünün tamamlanma durumu ve tamamlanan seans gösterilir. Kullanıcı aynı antrenmana tekrar başlamaya yönlendirilmez; “Seansı incele” bağlantısı vardır.
Devam eden seans varsa öncelik ona geçer. Güvenlik/plan durumu ana düğmenin davranışında önceliklidir.
Seansın neden önerildiği kısa bir açıklamayla gösterilir. Zaman bütçesi açıklaması seans detayına taşındı.
60 bilgi notu, yaş filtreleri ve uyku satırı korunur.

## Doğrulama
Hafif kaynak kontrolleri çalıştırılır. Ayrıca yalnız geometri, model ve profil kodu Kotlin ile 256 MB Java heap sınırında derlenerek kontrol edilir; bu tam Android derlemesi değildir.
Geometri kontrolü 1001 sprint örneğinde geçerli koordinatlar, sabit uzuv uzunlukları, döngü kapanışı ve geçersiz faz girdisini sınar. Mevcut 10 hareket döngüsü ve temel profil kuralları da kontrol edilir.
28 JUnit test tanımı korunmuş, mevcut geometri testi genişletilmiştir. Android JUnit/Compose testleri bu turda çalıştırılmamıştır.
APK, emülatör ve fiziksel TTS testi yapılmamıştır. Uzak bir ortamda çalıştırılmamıştır.
Bu sürümün çalıştırılan kontrollerinin sonucu KONTROL-SONUCU-0.6.txt dosyasındadır.
