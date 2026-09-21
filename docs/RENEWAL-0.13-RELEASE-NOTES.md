# Elevare 0.13 — görünür yenilikler ve teslim sınırları

## Kullanıcı yolculuğu

1. **Bugün:** bölüm ilerlemesi, tek ana antrenman kartı ve hemen altında günün bilgisi. Günlük yaşam destekleri bunların ardından gelir. Kömür–mercan Training Arc korunur.
2. **Plan atölyesi:** süre, haftalık gün, alan, odak ve destekler ayrı düzenlenir. Diğer yanıtlar açılır bölümde kalır. Kaydetmeden önce sonraki günlerin farkı gösterilir; açık seans sırasında program değişmez.
3. **İlerlemem:** bu döngü, son yedi gün ve tüm zamanlar ayrılır. Antrenman, nefes ve kısmi kayıtlar filtrelenir; eski kayıtlara sayfalı erişilir. Nefes ve yarım seans antrenman toplamını şişirmez.
4. **Hareket rehberi:** İzle / Tekniği öğren ayrımı; çizgi figürle birlikte yavaşlatma ve duraklatma. Mevcut animasyon geometrisi korunmuştur; yeni uzman onayı veya yeni hareket animasyonu iddia edilmez.
5. **Rutinler:** kısa kişisel not, haftalık değerlendirmeden düzenlemeye geçiş ve bütün hatırlatmaları tek yerden susturma. Rutin kaldırılırsa kendi notu ve adım kayıtları da kaldırılır; diğer rutinler korunur.
6. **Yedek:** dosyayı kontrol et → özetini gör → değiştirmeyi onayla. Birleştirme yapılmaz. Önceki kayıt için tek adımlık geri alma vardır. İçe aktarılan bildirimler kapalı, yarım seans duraklatılmıştır.
7. **Günlük uygunluk:** günlük ağrı yanıtı profil bilgisine dönüştürülmez. Tarih değişince otomatik temizlenmez; hedefli gözden geçirme gerekir.
8. **Ses:** profil içinden Türkçe sesi deneme ve mevcut cihaz sesi durumunu kontrol etme.

## Mantık düzeltmeleri

- Yeni döngü arşivi sonradan değişmiş haftalık planla tekrar sayılmaz.
- Uyku kaydı silinirken ilgili tamamlandı bilgisi de temizlenir.
- Haftalık Pro özeti tutarlı son yedi gün kapsamını kullanır.
- Hatırlatmalarda tek gelecek alarm ve eşzamanlı olaylar için belirli öncelik vardır. Dinlenme gününde gelecek antrenman hatırlatması kaybolmaz.
- Duraklatılmış yarım seans bütün bildirimleri süresiz engellemez.
- Aynı durum tekrar diske yazılmaz; saklanan ekran durumlarına sınır konur.
- Geri çekilmiş veya inceleme süresi dolmuş yayımlanmış bilgi kartları elenir. Eski kaynak özetleri uzman incelemesinden geçmiş gibi sunulmaz.

## Ne tamamlandı denemez?

38 bulgunun tek tek karşılığı `RENEWAL-38-STATUS.md` içindedir. Bir maddenin kodu bulunması, bütün kabul koşullarının sağlandığı anlamına gelmez.

- Eller serbest / ekran kilitliyken süren antrenman modu uygulanmadı. Mevcut rehber arka planda duraklar.
- Koşu, güç ve yoga programlarının uzman onay kapıları kapalı kaldı. Uygulamada hazırlık alternatifleri sunulur.
- 60 bilgi kartının bağımsız bilimsel incelemesi tamamlanmadı; envanter aracı içerik doğrulaması değildir.
- Gerçek ödeme ve otomatik yenileme yoktur. Üç günlük akış yalnız demodur; fiyat ve Play ürün kimlikleri beklenir.
- Fiziksel cihazda TalkBack, Bluetooth/arama, Doze, yeniden başlatma, üretici pil kısıtları ve düşük donanım performansı kabulü açık.
- 90/365/1000 kayıt ölçümleri emülatör içindir. Bazı eşzamanlı depolama işlemleri 100 ms üzerinde ölçüldü; tamamen takılmasız arayüz iddia edilmez.
- API 26 ve 33 koşusu seçilebilir; çalıştırılmamış matris hücreleri başarılı sayılmaz.
- Release iş akışı imzasız AAB üretir. İmza anahtarı, yükseltme testi ve Play iç test olmadan mağaza teslimi değildir.

## Doğrulama yaklaşımı

Yerel ağır Android derlemesi yapılmaz. Kaynak ve tasarım kontrolleri yerelde; Kotlin, birim testleri, ekran yolculukları, video ve yayın adayı kontrolleri GitHub Actions üzerinde yürütülür. Nihai çalışma kimlikleri ve sonuçları teslim notunda belirtilir.

Android 15'te izin geri alma hedef uygulama sürecini sonlandırdığı için süreç içinden yapılan izin değiştirme deneyi kaldırılmıştır. Bu, izin davranışının doğrulandığı anlamına gelmez; harici süreç/cihaz kabulü gerekir. Ekran testi, etiket metni yerine dokunulabilir kapsayıcıyı hedefler ve başarısız ekranda görüntü/hiyerarşi kanıtı saklar.
