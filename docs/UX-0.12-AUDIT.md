# Elevare 0.12 — İşleyiş ve arayüz denetimi

## Yaklaşım

0.11 kaynak kodu ve emülatör görüntülerinden başlayarak ilk kurulum, günlük kullanım, ayrıntıya girip geri dönme, kayıt düzeltme ve döngü sonu incelendi. Fiziksel cihazda kullanılmış gibi bir iddia yoktur. Yeni program/sağlık iddiası veya ödeme eklenmedi.

## Tespit → düzeltme

1. **Alt ekrandan geri dönüş bağlamı kayboluyordu.** Profil → Pro → geri, profil yerine ana sekmeye gidiyordu. Geri geçmişi eklendi; ekran ve Android geri düğmesi aynı davranır. Bildirimle giriş temiz geçmişle başlar; tamamlanan seans ana ekrana döner.
2. **Hareket listesinde filtre ve kaydırma sıfırlanıyordu.** Sayfa başına saklanabilir durum eklendi. Hareketten dönünce önceki liste ve konum korunur; uygulama yeniden oluşturulmasında da geri geçmişi saklanır. Yeni gün/döngü ayrı durum kullanır.
3. **Ana sekmelerde geri tuşu uygulamadan çıkarıyordu.** Rutinim/Rehber'den önce Bugün'e dönülür. Onboarding'in kendi zorunlu cevap ve geri davranışı korunur.
4. **90. gün erken kapanıyordu.** Kapanış sınırı 90. günün bitiminden sonraya alındı. Son gün açık kalır; o gün duraklatmak döngüyü tamamlamaz. Yeni döngü eski tarih/kayıtları koruyarak ancak süre tamamlanınca açılır.
5. **Dinlenme gününde süre uyarlama önizlemesi yanlış içerik gösteriyordu.** Bugünün dinlenme şablonu yerine sıradaki gerçek planlı antrenman gösterilir. Günü, adı ve gerçek süresi belirtilir. Duraklatılmış veya kalan antrenmanı olmayan döngüde açıklama ve kapalı uygulama düğmesi vardır.
6. **Seçili süre zemin üzerinde kayboluyordu.** Seçili kart mercan zemin, koyu yazı ve onay işareti alır; seçim yalnız renk ile anlatılmaz.
7. **Kapalı/plansız rutin için veri katmanı yeni kayıt kabul ediyordu.** Ekran kontrolüne ek olarak kayıt fonksiyonu da bu yazımları reddeder. Eski bir kaydı geri almak engellenmez.
8. **Rutini kapattıktan sonra yanlış kaydı düzeltmek mümkün değildi.** Yapıldı/atlandı kaydını kaldırma, rutin kapalı olsa da çalışır. Kapalı rutine yeni tamamlanma eklenmez.
9. **Katalogdan yeniden eklenen rutin eski bildirim tercihini açabiliyordu.** Katalog üzerinden yeniden ekleme bildirimleri kapalı başlatır; geçmişi korur. Gün/saat ekranındaki açık kullanıcı tercihleri ayrı kalır.
10. **Ana ekran açık kaldığında sabah/gün/akşam sıralaması eskimeye devam ediyordu.** Rutin zaman bağlamı 30 saniyede güncellenir; eski güne ait geri alma doğru kayıt tarihini kullanır.
11. **Azaltılmış harekette oynat/yavaşlat düğmeleri sonuç vermiyordu.** Sabit gösterim açıkça açıklanır; etkisiz animasyon kontrolleri gösterilmez. Teknik metin rehberi kullanılabilir.

## Doğrulama kapsamı

Yeni birim testleri: 90/91 sınırı; son günde duraklatma; dinlenme günü önizlemesi; kapalı/tamamlanmış plan; kapalı ve plansız rutin kaydı; bildirimleri sessiz yeniden ekleme. Android testi: Profil → Pro → uygulama yeniden oluşturma → geri → Profil; kök sekmeden Bugün'e dönüş. Mevcut hareket rehberi senaryosu artık geri döndüğünde korunmuş listeden devam eder.

Derleme, tüm birim testleri ve Android emülatör senaryolarının son kaynak sürümüyle sonucu ayrı teslim doğrulamasında belirtilir. Kaynak incelemesi, tek başına cihaz kabulü değildir.

## Bilinçli olarak açılmayanlar

Uzman onayı bekleyen antrenman dozları, profesyonel ses varlıkları ve gerçek Pro aboneliği bu çalışmayla hazır hale gelmiş sayılmaz. Kullanıcı testiyle ürün değerinin ve satın alma isteğinin ölçülmesi hâlâ gerekir. Sağlık/boy garantisi veya gizli ödeme eklenmedi.
