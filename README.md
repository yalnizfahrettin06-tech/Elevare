# Elevare Active 0.2.0

13–17 yaş için Türkçe, yerel Android egzersiz prototipi. Kotlin 2.0 ve Jetpack Compose ile geliştirildi; web sayfası sarmalayıcısı değildir.

## Kurulum

Android 8.0 ve üzeri gerekir. APK test anahtarıyla imzalanmıştır; mağaza yayını değildir. Dosyayı Android cihazında açıp yalnızca güvendiğin dosya kaynağına kurulum izni ver. Kurulumdan sonra bu izni tekrar kapatabilirsin. iPhone APK çalıştırmaz.

## Çalışan kapsam

- Spor odaklı mavi–lime arayüz, beş ana sekme, açık/koyu görünüm.
- 7 kısa seans, 10 hareket için şematik rehber; gerçek geri sayım, duraklatma ve bölüm geçişi.
- Uygulama arka plana geçtiğinde seans durur. Tamamlanmayan seans başarı olarak yazılmaz.
- Arama, kategori filtreleri ve favoriler.
- Tamamlanan seans geçmişi, son 7 gün grafiği, rutine dayalı küçük kazanımlar.
- Ara verilebilen 90 günlük alışkanlık takvimi. Kişiye özel veya giderek ağırlaşan egzersiz programı değildir.
- Elle uyku günlüğü, gece yarısını geçen süre hesabı, uyku planı.
- Yerel kayıtlar, JSON dışa aktarım ve onaylı veri silme.
- Azaltılmış hareket, dokunma hissi ve daha hafif öneri tercihleri.

Hesap, reklam, analitik, ödeme, bulut, yapay zekâ sohbeti, bildirim ve Health Connect entegrasyonu yoktur. Sensörle uyku veya aktivite ölçümü yapılmaz. Kayıtlar cihazdan çıkmaz; dışa aktarım yalnızca kullanıcı seçimiyle yapılır. Uygulamayı kaldırmak verileri siler.

## İçerik sınırları

Boy uzaması, GH artışı, kilo veya kalori hedefi yoktur. Seanslar genel örneklerdir; tıbbi tavsiye veya uzman onaylı kişisel program değildir. Sağlık durumuna özel uygunluk uzmanla değerlendirilmelidir. İlk denemede ebeveyn/öğretmen/antrenör desteği önerilir. Ağrı, baş dönmesi veya rahatsızlıkta durulmalıdır.

Kısa seanslar gün içindeki tüm hareketin yerine geçmez. Bazı seanslar sabit sandalye ve duvar/destek gerektirir. Uyku günlüğü yatakta geçen tahmini süreyi gösterir; gerçek uyku ölçümü değildir.

## Tasarım araştırması

Antrenman öncelikli kartlar, belirgin süre/ekipman bilgisi, kolay filtreleme ve tek ana eylem yaklaşımı örnek uygulamaların incelenmesinden çıkarılan tasarım kararlarıdır. Renkler Elevare için seçilmiştir; diğer markaların renkleri kopyalanmamıştır.

- [Freeletics antrenman kapsamı](https://www.freeletics.com/en/bodyweight-training/)
- [Freeletics başlangıç rehberi](https://help.freeletics.com/hc/en-us/articles/115004675229-Get-started-with-Freeletics-Training)
- [Nike Run Club](https://www.nike.com/help/a/ntc-nrc/nrc-runs)
- [WHO fiziksel aktivite rehberi](https://www.who.int/publications/i/item/9789240014886): 5–17 yaş için hafta boyunca günlük ortalama en az 60 dakika orta-yüksek şiddette, çoğunlukla aerobik aktivite.
- [CDC yaşa uygun hareket](https://www.cdc.gov/physical-activity-basics/adding-children-adolescents/what-counts.html)
- [AASM gençlerde uyku](https://aasm.org/advocacy/position-statements/teen-sleep-duration-health-advisory/): 13–18 yaş için 24 saatte 8–10 saat.

Bu kuruluşlar uygulamayı veya seanslarını onaylamış değildir. Kaynak inceleme tarihi: 6 Eylül 2026.

## Görsel üretimi

Imagegen becerisi ile yeni bir spor sahası kapak fotoğrafı üretildi, görsel incelenerek uygulamaya eklendi. Görsel yapay zekâ üretimidir ve yetişkin spor eğitmenlerini temsil eder; gerçek eğitmen tavsiyesi veya hareket tekniği gösterimi değildir. Asset: `app/src/main/res/drawable-nodpi/sports_cover.png`. Düzenleme değil yeni üretim modu kullanıldı.

Üretim yönlendirmesi: gündüz mavi basketbol sahasında, lime ve kobalt spor kıyafetleriyle rahat yana adım atan iki yetişkin spor eğitmeni; enerjik, gündelik spor fotoğrafı; yazı, logo ve filigran yok. Hareket rehberleri uygulama içinde kodla çizilmiş şemalardır.

## Geliştirme

JDK 17, Android SDK platform 35 ve Gradle 8.7 gerekir. `local.properties` içinde kendi SDK yolunu tanımla veya ANDROID_HOME kullan. `gradle :app:assembleDebug :app:testDebugUnitTest` ile derle. Paket: `com.elevare.active`, sürüm: `0.2.0`, minimum API 26, hedef API 35.

Yayına çıkmadan önce fiziksel cihazlar ve erişilebilirlik testleri, uzman hareket içeriği incelemesi, ürün/gizlilik incelemesi ve ayrı release imzalama süreci gereklidir. Debug anahtarı üretim anahtarı olarak kullanılmamalıdır.
