# Elevare 0.4.0 — Sade arayüz

Bu sürümün amacı uygulamayı açıp antrenmana başlamayı kolaylaştırmaktır.

## Değişiklikler

- Ana ekran: bugünkü antrenman, başlat düğmesi, tek bilgi kartı ve uyku kısayolu. Takvim ve ilerleme Profil'de.
- Başlat düğmesi kısa hazırlık onayından sonra seansı açar. Açık seans varsa üzerine yazmaz.
- Onboarding beş adım: tanıtım, yaş, isteğe bağlı boy bilgileri, süre tercihi ve deneme. Her adım ayrı kaydırma durumuyla üstten açılır.
- Sadece 3 günlük demo. Eski 7 günlük tercih 3'e normalleştirilir. Ödeme, abonelik ve otomatik yenileme yoktur.
- Bilgi listesinde kısa başlıklar; PMID, bulgu ve sınırlılıklar detay sayfasında.
- Kompakt antrenman kartları ve açılır hareket rehberi. Favori ekleme antrenman detayında.
- Profilde kişisel bilgiler, uyku/bildirim, görünüm ve gizlilik açılır gruplar halinde.
- 2D çizimler korunur. Bunlar hareketin bütün evrelerini gösteren animasyonlar değildir.

## Kurulum

Android 8.0+; paket com.elevare.active, sürüm kodu 4. Test imzalı APK, mağaza sürümü değildir. Önceki test sürümünün üzerine güncellenebilir; uygulamayı kaldırmak yerel kayıtları siler. Önemli kayıtları önce dışa aktarın.

Mevcut 0.3 kullanıcısının onboarding'i zorla tekrarlanmaz. Yeni akışı Profil > Kişisel bilgiler > Yanıtlarımı düzenle yoluyla görebilirsiniz. Boy alanları isteğe bağlıdır. Günlük süre, kişisel rutin tercihidir; seçilen seansların toplam süresini otomatik olarak uzatmaz.

## Bildirim ve veri

Bildirimler isteğe bağlıdır; Profil > Uyku ve bildirimler. Uyku planından yaklaşık 30 dakika önce planlanır; Android pil yönetimi geciktirebilir. Kayıtlar cihazda tutulur. Firebase, internet izni, gerçek Pro üyeliği ve sensör ölçümü bulunmaz. PubMed harici tarayıcıda açılır.

## Derleme

JDK 17, Android SDK 35, Gradle 8.7. SDK yolunu local.properties veya ANDROID_HOME ile ayarlayın.

    ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --max-workers=1

Kaynak arşivi imza anahtarını ve yerel SDK yollarını içermez. Önceki README belgeleri sürüm tarihçesidir; bu sürüm için README-0.4.md geçerlidir.

## Sınırlar

Bu UI/UX revizyonu yeni tıbbi iddia eklemez. Maksimal sprint yerine koşuya hazırlık seansı vardır. Boy garantisi, görünüş puanı, genital güneşlendirme veya tehlikeli nefes tutma görevi yoktur. Ayrıntılı inceleme Elevare-04-Sadelestirme-Raporu.md dosyasındadır. Cihaz/görsel test durumu teslimdeki test notlarında ayrıca belirtilir.
