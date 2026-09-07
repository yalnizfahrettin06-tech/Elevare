# Elevare 0.7.0

Türkçe, native Android workout uygulaması. Koyu gece mavisi arayüz, çizgi adam animasyonları ve yanıtlara göre üretilen haftalık program.

- 13–21 yaş; zorunlu 9 soru; 5/10/15/30 dakika ve haftada 2/3/4 gün.
- Ortam, deneyim, uyku, süre ve öncelik programı belirler. Hazırlık akışı 30 saniye; sonuç ekranında tek 3 günlük demo eylemi.
- Koşu günlerinde ısınma, kısa kontrollü hızlanma ve yürüyüş intervalleri. Toplam süre ısınma/soğumayı içerir. 5 dakikalık plan koşuya hazırlıktır.
- Yoga: kedi–inek, çocuk pozu ve alçak hamle. Güç ve toparlanma günleri.
- Sürekli koşu döngüsü; rehberde durdurma/yavaşlatma, erişilebilir azaltılmış hareket.
- Otomatik bölüm geçişi, Türkçe çevrimdışı sesli koç, son saniye sesi; arka planda/kulaklık ayrılınca duraklama.
- 60 kaynaklı bilgi kartı, yerel uyku hatırlatıcısı, seans geçmişi ve JSON dışa aktarım.
- Hesap/ödeme/Firebase yok. Demo ücret veya otomatik yenileme başlatmaz.

## Derleme

JDK 17, Android SDK 35. `bash gradlew :app:assembleDebug :app:testDebugUnitTest`.
GitHub Actions APK, test raporları ve Android emülatör ekran görüntülerini oluşturur. Yerel emülatör gerekli değildir.

## İnceleme

[0.7 UI/UX raporu](docs/UI-UX-0.7-RAPORU.md). Ana ekran ile haftalık liste aynı program oluşturucuyu kullanır. Kalıcı program kimliği seans yeniden açıldığında aynı adımları çözer.

Boy tahmini veya hormon ölçümü yoktur. Programlar genel başlangıç şablonlarıdır; uzman onaylı kişisel egzersiz reçeteleri değildir. Sprint interval süresi yürüyüş aralarını da içerir.

Paket `com.elevare.active`, min API 26, hedef API 35. Debug APK test imzalıdır.
