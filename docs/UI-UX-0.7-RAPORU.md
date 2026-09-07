# Elevare 0.7 — Workout deneyimi incelemesi ve uygulama planı

Bu rapor kullanıcının son yedi ekran görüntüsü ve 0.6 kaynak kodu incelenerek, uygulama değişikliklerinden önce hazırlanmıştır. Görüntüler hareketin akıcılığını kanıtlamaz; animasyon değerlendirmesi kodun zamanlama ve eklem hesaplarından gelir.

## 1. Sorunların kökü

| Öncelik | Bulgu | Kullanıcıya etkisi | 0.7 kararı |
|---|---|---|---|
| P0 | `UserState.dark=false`, Android açılış teması da açık | Baştan sona istenmeyen beyaz ekran; ayar değiştirmek gerekir | Lacivert-antrasit spor teması, açılış dahil koyu; eski kayıtlara tema geçişi |
| P0 | Program yalnızca flow/strength/runprep seçiyor | 15 dakika seçen kişi 5,5 dakikalık hazır seans görüyor | Süreyi tam dolduran, ortam/deneyim/gün/uyku/öncelik girdili haftalık program |
| P0 | Sprint yalnızca rehberde; seansa dahil değil | Ürünün temel vaadiyle antrenman içeriği uyuşmuyor | Uygun koşu günlerinde hızlanma-yürüyüş intervalleri; aralarda toparlanma |
| P0 | Hazırlık bittiğinde denemeye geç, sonraki ekranda yeniden dene | Aynı kararı iki kez verdiriyor | 30 saniye hazırlık otomatik olarak program sonucuna geçer; tek deneme başlatma düğmesi |
| P1 | Sprint dört kare, doğrusal açı geçişleri ve 1,8 saniye döngü | Robotik pozlar ve beklemeli koşu | Sürekli koşu fazı, sabit segmentler, yere basan ayak, uçuş ve kalça salınımı |
| P1 | Yoga akışı yok; birkaç ayakta hareket tekrar ediyor | Hareket çeşitliliği düşük, figürün ne yaptığı anlaşılmıyor | Kedi-inek, çocuk pozu, alçak hamle ve ayakta açılma; yavaş ayrı fazlar |
| P1 | Her adım sonunda sonraki hareket için dokunmak gerekiyor | Koşarken telefonla uğraşmak zorunda kalınıyor | Açıkken otomatik bölüm geçişi, Türkçe koç ve son saniye sesleri |
| P1 | Ana ekranda teknik eşleştirme açıklaması var | Spor odağını dağıtıyor, bir yazılım demosu hissi veriyor | Bugünkü iş, toplam süre, interval özeti, başlat düğmesi ve tek bilgi kartı |
| P1 | Tüm kartlar aynı büyüklük ve aynı formda | Başlangıç düğmesiyle ikincil ayarlar yarışıyor | Antrenman başrol; bilim/uyku kompakt, profil gruplanmış |
| P1 | Seansa başlangıçta koşu alanı bilgisi alınmıyor | Evdeki kullanıcıya koşu önerilebilir | Zorunlu ortam sorusu; ev planında sprint yok |
| P1 | Onboarding geri/ileri geçişi sert | Uzun form hissi, devamlılık kaybı | Yönlü kısa geçişler, sabit alt düğme, her soruda tek karar |
| P2 | Büyük boşluklar var ama içerik hiyerarşisi zayıf | Boş ekran hissi | Seçeneklerin üstünde küçük adım etiketi; amaçlı figür ve kısa yardımcı metin |
| P2 | Profil başlığı, Senin alanın ve seans sayısı ayrı bloklar | Gereksiz dikey uzama | Tek profil özeti ve kompakt ilerleme alanı |
| P2 | Bilgi kartı uzun klinik cümleyle açılabiliyor | Antrenmanla bağlantı zayıf | Günlük başlangıç kartını egzersiz/uyku/beslenme havuzundan seçme |

## 2. Görsel yön

Arka plan gece mavisi (#101D2D), kartlar aydınlık lacivert (#1B2D44), aktif eylem kobalt, koşu vurgu rengi mercan. Metin sıcak açık gri; büyük beyaz zeminler yok. Parlak vurgular sınırlı alanda kullanılır. Altın, satış odaklı parıltı ve neon sarı-yeşil kullanılmaz. Buton minimum 54 dp, diğer dokunma alanları minimum 48 dp. Başlıklar çoğunlukla 24–32 sp; her şeyi büyük harfle yazmak yerine kısa eylem başlıkları kullanılır.

Taller'ın resmi mağaza sayfasındaki yaşam tarzı soruları → plan anlatımı ürün akışı için referanstır. Boy tahmini iddiaları devralınmaz. Nike Training Club'ın antrenman/program odağı, süre ve antrenman türünü okunur tutması referanstır. Ekranlar ya da marka görselleri kopyalanmaz.

- Taller: https://play.google.com/store/apps/details?id=com.virtualnetwork.taller
- Nike Training Club: https://www.nike.com/ntc-app

## 3. Yeni ana ekran

Üstte küçük marka ve haftanın günleri. İlk kartta bugünün antrenmanı, süre, koşu/interval özeti ve hareketli çizgi koşucu. İlk ekranda görünen tek baskın eylem “Antrenmana başla”. Altında “Sprint nedir?” veya o güne ait hareket rehberi bağlantısı. Koşu süresi, yürüyüş aralarını içeren interval bloğu olarak ayrıca etiketlenir. Isınma ve soğuma toplam süreye dahildir.

Haftalık program koşu, güç/yoga ve dinlenmeyi ayırır. Bir sonraki günün adı, kullanıcının bugünkü işini değiştiren bir ikinci başlat düğmesine dönüşmez. Tamamlanan seans kaydı ayrı durum olarak görünür. Duraklatılan seans program yeniden hesaplanınca kaybolmaz.

## 4. Onboarding

Karşılama → yaş (13–21) → öncelik → hareket deneyimi → antrenman ortamı → haftalık gün (2/3/4) → uyku → büyüme gözlemi → günlük süre (5/10/15/30) → uygunluk → 30 saniyelik hazırlık → program özeti + tek 3 günlük deneme düğmesi.

Tüm sorular yanıtlanmalıdır; belirsiz sağlık/büyüme bilgisi için “bilmiyorum” gerçek bir yanıttır. Yaş, takvim kapasitesi ve hareket deneyimi farklı işe yarar. Büyüme gözlemi bilgi içeriğini etkiler; egzersiz şiddetine ya da kalan boy tahminine çevrilmez. Eklemlerin görünüşünden hormon/büyüme plağı değerlendirilmez.

Hazırlık aşamaları: tercihler değerlendiriliyor → antrenman günleri hesaplanıyor → hareket listesi oluşturuluyor → program hazırlanıyor. Gerçek kural tabanlı program bu aşamada hesaplanır. 30 saniye sunum akışıdır; hormon testi veya tıbbi analiz gösterimi değildir. Arka plana gidildiğinde sunum ve egzersiz sayacı durur. Sonuç ekranındaki program ile ana ekran aynı veri modelini kullanır.

## 5. Program mantığı ve koşu içeriği

Kısa hızlı koşular yürüyüş/toparlanma aralarıyla ayrılır. “5–10 dakika sprint” kesintisiz maksimum efor olarak uygulanmaz. Başlangıç seviyesinde daha kısa hızlanma, daha uzun yürüyüş; daha düzenli yetişkinde kontrollü daha uzun hızlanma. 5 dakikalık bütçe koşuya hazırlık içerir; zorunlu ısınma ve toparlanmayı keserek sprint sıkıştırılmaz. Ev seçen kullanıcıya düşük darbeli güç/yoga hazırlanır. Çok az uyku veya hafif tercih, o günün programını yumuşatır. Ağrı/kısıtlama/belirsizlikte zamanlı seans kilidi korunur.

Koşu günleri bitişik yerleştirilmez; kayıtlı son koşunun ardından toparlanma uygulanır. Haftalık plan bir başlangıç şablonudur. Bu özel interval saniyeleri herhangi bir kuruluşun onayladığı reçete değildir. Isınma, yürüyüş-koşu geçişleri ve koşular arasında dinlenme ilkeleri NHS rehberinden yararlanır: https://www.nhs.uk/better-health/get-active/get-running-with-couch-to-5k/couch-to-5k-running-plan/

## 6. Figür ve antrenman oynatıcısı

Figür basit çizgi adam kalır. Koşuda yandan görünüş, karşı kol-bacak, gövde öne eğimi, destek ayağı ve havadaki bacağın diz kıvrımı aynı döngüde çalışır. Arka uzuv daha düşük kontrastlı; ön uzuv belirgin. Zemindeki pist izleri hareket hissi verir. Animasyon örnek kadanstır; kişiye bir koşu hızı dayatmaz.

Yoga için hızlı koşu döngüsü kullanılmaz: pozlar arasında yumuşak geçiş ve tutuş vardır. Figür, metindeki hareketle eşleşir. Rehberde animasyonu durdurma ve yavaş izleme; antrenmanda duraklatıldığında hem sayaç hem figür donar. Hareket azaltma tercihi korunur. Koşu intervalleri otomatik ilerler; otomatik geçişi kapatmak mümkündür. Kulaklık ayrılınca veya uygulama arka plana gidince mevcut duraklama davranışı korunur.

## 7. Kabul kontrolleri

- Açılış, onboarding, ana ekran, seans ve sistem çubukları koyu temada uyumlu.
- Hazırlık tam 30 saniye; sonunda tek deneme eylemi.
- Süre seçeneklerinin ürettiği seanslar belirtilen bütçeye uyar.
- Ev/uygunsuzluk/az uyku durumları koşu programına düşmez.
- Haftalık koşu günleri bitişik değil; seans kimliği yeniden açılışta aynı programı çözer.
- Koşu döngüsünün sınırları sürekli; segment uzunlukları sabit ve koordinatlar taşmıyor.
- Otomatik geçiş, duraklatma, tamamlanma ve tek kayıt oluşturma test edilir.
- Derleme ve testler GitHub ortamında; yerel bilgisayarda Android emülatörü çalıştırılmaz.
- Son APK'nin ekranları sanal Android cihazından alınarak görsel kontrol yapılır; tarayıcı maketi Android testi yerine sunulmaz.
