# Elevare 0.10.0 · Training Arc / Yaşam ritmi

[0.10 kapsam ve kabul notları](docs/LIFESTYLE-0.10-DELIVERY.md): Bugün / Rutinim / Rehber, altı isteğe bağlı destek rutini, geri alınabilir günlük kayıtlar, haftalık değerlendirme ve ortak bildirim bütçesi. Antrenman ana eylem olarak korunur. Vitamin, doz, sağlık puanı, bulut ve ödeme eklenmez.

Türkçe, native Android workout uygulaması. Ana plan v1.0 temel alınarak hazırlanmış; Training Arc kimliği, sıcak kömür/mercan paleti, özgün çizgi ikonlar ve vektör atlet. Zorunlu onboarding ve 90 günlük program motoru korunur.

[Training Arc tasarım ve teslim notları](docs/TRAINING-ARC-0.9-DELIVERY.md). Bu sürüm görsel katmanı yeniler; yeni antrenman protokolü veya sağlık iddiası eklemez.

- 13–21 yaş; zorunlu 11 soru; 5/10/15/30 dakika ve haftada 2/3/4 gün. Boy/hedef boy veya görüntüden hormon tahmini yok.
- Kaydedilen form taslağı, 30 saniyelik ön planda hazırlık, tek sonuç ekranında tek 3 günlük demo. Profil düzenlemek süreyi veya demoyu yeniden başlatmaz.
- 90 gün / 5 faz, hafif haftalar, ekipmana ve ortama uyarlama, seviye onayı, ara sonrası dönüş ve döngü arşivi.
- Ana ekran: tek ana eylem, bir bilgi kartı, uyku ve program kısayolu. Tamamlanmış, yarım kalmış, dinlenme, duraklatılmış ve dönem sonu durumları ayrı.
- P/R/G/Y/K şablonları, kesin süre hesapları, ısınma ve soğuma. 5 dakika, kesintisiz sprint anlamına gelmez.
- Basit çizgi adam; koşu/yürüyüş ayrımı, yoga giriş–bekleme–çıkışı, yavaş gösterim ve azaltılmış hareket.
- Türkçe çevrimdışı sesli koç; ses odağı/kulaklık ayrılma kontrolü; monoton saatli sayaç ve duraklatılmış geri yükleme.
- 60 mevcut kaynaklı bilgi kartı, kaydetme ve tekrar azaltan seçim; uyku/antrenman bildirimleri; geçmiş, kısmi seanslar ve JSON dışa aktarım.
- Hesap/ödeme/Firebase yok. Demo ücret veya otomatik yenileme başlatmaz.

## Önemli: inceleme kapıları

Yeni koşu, güç, yoga ve kombine protokollerinin uzman onayı uydurulmadı. `ProgramCycle.kt` içindeki `RELEASE_PROGRAM_APPROVALS` kapalıdır: uygulama inceleme bekleyen takvim adayını ve oynatılabilir hazırlık alternatifini ayrı gösterir. Uygulama bunları tıbbi olarak onaylanmış gibi başlatmaz. Bu sürüm mağaza yayınına hazır veya cihaz testleri tamamlanmış olarak sunulmaz.

## Derleme

JDK 17, Android SDK 35. `bash gradlew :app:assembleDebug :app:testDebugUnitTest`.
GitHub Actions iş akışı, elle başlatıldığında APK, test raporları ve Android emülatör kanıtları üretmek üzere hazırlanmıştır. Bu güncellemede uzaktaki iş akışı başlatılmadı. Yerel emülatör gerekli değildir.

## İnceleme

[0.8 uygulama ve doğrulama raporu](docs/MASTER-PLAN-0.8-IMPLEMENTATION.md). Ana ekran ve program aynı motoru kullanır. Eski `p7` kimlikleri korunur; yeni `p8` seansları tam hareket anlık görüntüsüyle saklanır.

`scripts/verify-source.ps1` yalnız statik kaynak kontrolüdür; test veya APK derlemesi yerine geçmez. Üretim geometrisinden bağımsız inceleme çıktısı almak için `scripts/MotionPreview.kt` bulunur; çıktı bir Android ekran görüntüsü değildir.

Boy tahmini veya hormon ölçümü yoktur. Programlar genel başlangıç şablonlarıdır; uzman onaylı kişisel egzersiz reçeteleri değildir. Sprint interval süresi yürüyüş aralarını da içerir.

Paket `com.elevare.active`, min API 26, hedef API 35. Debug APK test imzalıdır.
