# Elevare 0.3.0

Türkçe, 13–17 yaş odaklı yerel Android prototipi. Grafit–mor arayüz, çizimli hareket referansları ve kaynaklı bilim kütüphanesi. Marka alt başlığı “Maximize Your Growth”; boy garantisi değildir.

## Bu sürüm

- Dört ana sekme: Bugün, Antrenman, Bilim, Profil. Takvim ve ilerleme alt sayfalarda.
- 7 aşamalı, geri gidilebilir onboarding: tanıtım, yaş, isteğe bağlı mevcut/hedef boy, süre, bilim kartları, başlangıç şablonu, deneme önizlemesi.
- 3/7 günlük demo seçimi. Ödeme alınmaz, gerçek abonelik veya süreli kilit açılmaz. Atlanabilir.
- 8 kısa seans: koşuya hazırlık, mevcut destekleyici antrenmanlar ve nefes. Koşuya hazırlık, maksimal sprint deneyi değildir.
- 10 statik 2D hareket çizimi; eski çubuk figürlerin yerini alır. Çizimler tüm hareket evrelerini temsil etmez.
- 6 bilim kartı: bulgu, örneklem, sınır, PMID ve PubMed bağlantısı. İçerik çevrimdışı; dış PubMed sayfası tarayıcıyla açılır.
- Yerel uyku hatırlatıcısı: kullanıcı açarsa, uyku planından yaklaşık 30 dakika önce Android bildirimi planlanır. Pil tasarrufu geciktirebilir. İzin reddi desteklenir.
- Yerel kayıtlar, favoriler, sayaç, duraklatma, günlük, dışa aktarım ve veri silme korunur.

Firebase, Play Billing, bulut hesabı, görünüş puanı, boy tahmini, gerçek Pro aboneliği veya hormon ölçümü bulunmaz. Büyüme amaçlı genital güneşlendirme, hiperventilasyon veya maksimal nefes tutma protokolü yoktur.

## Kurulum ve güncelleme

Android 8.0+ gerekir. Elevare-0.3.0.apk test anahtarıyla imzalıdır; mağaza sürümü değildir. Aynı paket kimliği (com.elevare.active) ve yüksek sürüm kodu (3) kullanır. Önceki test APK’sıyla aynı imza olduğu doğrulanırsa üzerine güncellenebilir. Kaldırıp yeniden kurmak kayıtları siler; önce dışa aktarım yap.

Eski yerel kayıtlar için ek alanlar varsayılanlarla okunur. Yeni onboarding bir kez açılır; eski seans ve uyku geçmişi silinmez. Hedef boy yalnız kişisel not olarak tutulur. Sayısal değerleri paylaşmak zorunlu değildir.

Bildirimler varsayılan kapalıdır. Profil > Uyku hatırlatıcısı üzerinden etkinleştirilir. Android 13+ izin sorar. Kesin alarm izni kullanılmaz; dakika hassasiyetinde teslim vaat edilmez. Cihazı yeniden başlatınca etkin tercihler yeniden planlanır.

## Derleme

JDK 17, Android SDK 35 ve Gradle 8.7. local.properties dosyasını kendi SDK yolunla oluştur veya ANDROID_HOME ayarla.

    ./gradlew :app:assembleDebug :app:testDebugUnitTest --max-workers=1

Önceki dosyalar için README.md 0.2 tarihçesidir; bu sürüm için bu belge esas alınır. app/src/test birim testlerini içerir. İmza anahtarı kaynak arşivine dahil edilmez.

## Araştırma

Ayrıntılı ekran incelemesi ve referans değerlendirmesi teslimdeki Elevare-03-UIUX-ve-Arastirma-Raporu.md dosyasındadır. PubMed kimlikleri: 12137178, 16374019, 3100467, 36630953, 34433056, 30839054, 31089738. Geçici hormon yanıtı, doğrusal büyüme ve kemik mineral içeriği ayrı tutulur. Kaynak kuruluşları uygulamayı onaylamış değildir.

## Görsel varlık

Imagegen becerisi, yerleşik yeni üretim modunda kullanıldı. Seçilen varlık: app/src/main/res/drawable-nodpi/coach_illustrations.png. Gerçekçi/3D ilk taslak kullanılmadı. Final yönlendirme, aynı yetişkin eğitmenin mor ve grafit spor kıyafetleriyle 2 sütun × 5 satır halinde on farklı duruşunu, düz renkli 2D editoryal çizim olarak üretmekti. Ayrı fotoğraflar veya gizli bir video oynatıcı yoktur; hücreler Android Canvas üzerinde gösterilir.

Tam görsel yönlendirmesi ART-PROMPT.txt içindedir. Üretilen çizimler gözle incelendi; uzman hareket analizi yapılmadı.

## Yayına çıkmadan önce

Fiziksel cihazlar, büyük yazı, TalkBack, küçük ekran, arka plan/yeniden başlatma ve bildirim teslimi testleri; çocuk/genç kullanıcı gizliliği ve ödeme akışı incelemesi; sağlık/egzersiz uzmanı içerik değerlendirmesi; gerçek mağaza imzası gereklidir. Sadece derleme veya birim testi başarısı bu kontrollerin yerine geçmez.
