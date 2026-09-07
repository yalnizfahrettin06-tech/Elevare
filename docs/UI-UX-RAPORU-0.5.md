# Elevare 0.5 — Arayüz analizi ve uygulama raporu
7 Eylül 2026 · Kaynak kod teslimi · APK üretilmedi

## 1. Ekran görüntülerinde tespit edilen sorunlar
- Ana görev ile açıklamalar aynı ağırlıktaydı. Kullanıcı antrenmana ulaşmadan çok sayıda metin, kart ve seçenek okumak zorunda kalıyordu.
- Onboarding bilgilendirme makalesi gibi ilerliyordu. Aynı uyarı farklı ekranlarda tekrar ediliyor; açıklamalar seçimlerden daha fazla yer tutuyordu.
- Sabit üst/alt alanların arasındaki içerik bazı görüntülerde kesiliyordu. Yeni akışta içerik kendi kaydırma alanında, devam düğmesi ayrı alanda tutuldu; adım değişiminde kaydırma yenilenir. Gerçek cihazda büyük yazı testi henüz yapılmadı.
- Resimlerin çizilmiş olması yeterli değildi: kullanıcı basit çizgi simgesi istiyordu, mevcut insan illüstrasyonu hâlâ gerçekçi karakter algısı yaratıyordu.
- Koyu mor yüzeyler, parıltı simgeleri ve iri kartlar egzersizden çok abonelik ürünü hissi veriyordu.
- Yaş seçimi 17'de bitiyordu. Süre seçimi ile önerilen seans arasında belirgin bağ yoktu.
- Ana ekrandaki sabit süt konusu tekrar ediyordu. Bilginin kendisi yerine makaleye yönlendirme öne çıkıyordu.
- Sesli koç ve süre sesi yoktu. Ağrı bildirimi yalnız sonraki öneriyi hafifletiyordu.
- Eski ekran uygulamaları kaynakta kalmıştı. Kullanılmayan ilk onboarding ve fotoğraflı eski ana ekran kodu kaldırıldı.

## 2. Yeni ana ekran
Öncelik sırası: Bugünkü seans → Antrenmana başla → Bugünün bilgisi → Uyku planı.
Üstte büyük pazarlama paragrafı, görünüş puanı, hormon puanı veya hedef santimetre yok.
Seans kartı: isim, gerçek süre, hareket sayısı, küçük çizgi figürü ve belirgin mavi düğme.
Hareket listesi ikincil bir bağlantıdır. Açık seans varsa düğme “Seansa devam et” olur.
Bilgi kartı doğrudan kısa cümleyi gösterir; tıklanınca kaynak, katılımcılar, bulgu ve sınırlar açılır.
Kart tarih ve yaş grubuna göre döner; “Değiştir” ile başka bir not okunabilir.
Uyku planı tek satırdır. Takvim ve ilerleme profil altında kalır.

Yeni kurulumda açık, spor odaklı tema: buz mavisi zemin, beyaz kart, güçlü mavi ana düğme, koyu mürekkep çizgiler. Sarı/yeşil vurgu yok. Önceden koyu görünüm seçen kişinin tercihi korunur; Profil'den değiştirilebilir.
Asgari ana düğme yüksekliği 54 dp, seçim satırı 52 dp. Küçük yazılarda uzun açıklamalar yerine detay ekranı kullanılır. Büyük yazı ve dar ekran erişilebilirliği son cihaz kontrolü gerektirir.

## 3. Çizgi adam
Android Canvas ile baş çemberi, gövde ve eklem çizgileri çizilir. Yüz, kıyafet, ten veya gerçekçi insan dokusu yok.
10 hareket kimliği için pozlar tanımlandı. Sandalye, duvar ve destek gerektiğinde ince çizgilerle gösterilir.
Liste kartlarında sabit, seans içinde basit iki poz arasında geçiş vardır. Duraklatma veya azaltılmış hareket tercihi animasyonu durdurur.
Bunlar şematik açıklamalardır; biyomekanik doğrulama veya antrenör değerlendirmesi yerine geçmez.
Eski bitmap insan görselleri yeni teslim paketine alınmadı; çalışma dizinindeki eski dosyalar silinmedi.

## 4. Zorunlu onboarding
Karşılama + 7 soru + hazırlık + 3 günlük demo ekranı:
1. Yaş: 13–21, dokuz anlaşılır seçenek.
2. Öncelik: güç/kondisyon; toparlanma/rutin; uyku/enerji; büyüme ve GH hakkında bilgi.
3. Son 6 ayda boy değişimi: evet / fark etmedim / ölçmedim-bilmiyorum.
4. Genel uyku süresi.
5. Haftalık hareket sıklığı.
6. Günlük zaman bütçesi: 5 / 10 / 15 / 30 dakika.
7. Antrenmanı etkileyen ağrı, uzman kısıtlaması veya belirsizlik.

Geçerli seçim olmadan devam düğmesi etkinleşmez. Son kayıt öncesinde tüm yanıtlar tekrar doğrulanır.
“Bilmiyorum” geçerli bir yanıttır; kullanıcıyı ölçmediği bir sağlık bilgisini uydurmaya zorlamaz.
Mevcut boy, hedef boy veya köprücük kemiği fotoğrafı sorulmaz. Dış görünüşten büyüme plakları, kemik yaşı veya GH düzeyi çıkarılmaz.
Eski sürüm yanıtları olan kullanıcı yeni soruları tamamlar; geçmiş seanslar silinmez. Eski boy alanları yalnız veri geçişi için saklanır, yeni arayüzde gösterilmez.
Yanıt düzenleme başlamadan açık seans duraklatılır.

## 5. Hazırlık ekranı ve deneme
45 saniye boyunca üç aşamalı hazırlık gösterilir. Uygulama arka plana giderse süre ilerlemez.
Gerçek işlem cihaz içindeki basit kurallarla mevcut seansların eşleştirilmesidir; 45 saniyelik tıbbi hesaplama yapılıyormuş gibi sunulmaz. Gösterimin niteliği ekranda açıklanır.
Tamamlandığında önerinin nedeni ve gerçek ilk seans süresi görünür; ardından yalnız 3 günlük demo ekranı vardır.
7 günlük deneme seçeneği yoktur. Demo ödeme almaz, gerçek abonelik başlatmaz, süre sonunda otomatik ücretlendirme veya kilitleme yapmaz.
Demo atlanabilir; zorunlu olan anket yanıtlarıdır. Ödeme entegrasyonu olmadan zorunlu satın alma izlenimi oluşturulmaz.

## 6. Kişiselleştirme gerçekten ne yapıyor?
Öncelik, hareket deneyimi, uyku ve süre kısa başlangıç seansını seçer. Yeni başlayan veya az uyku bildiren kişiye daha hafif şablon; uygun durumda güç odaklı kişiye temel güç seansı seçilir.
30 dakika tercihi 30 dakikalık yoğun antrenmana dönüştürülmez: ilk seansın gerçek süresi açıkça belirtilir, kalan zaman isteğe bağlıdır. 10/15/30 tercihlerinde aynı kısa ilk seans çıkabilir. Bu sürümde 30 dakikalık yeni program yazılmadı.
Büyüme gözlemi bilgi kartlarının sıralamasını etkiler; kalan boy, ergenlik aşaması veya hormon seviyesi hesaplanmaz.
Ağrı/kısıtlama/belirsizlik yanıtında sayaçlı seanslar kapalıdır; bilgi içerikleri açıktır. Seans sonunda rahatsızlık kaydı da sonraki seansları uygunluk yeniden değerlendirilene kadar engeller.
Bu eşleştirme klinik tarama veya “egzersize uygundur” onayı değildir.

## 7. Bilgi havuzu
60 farklı kısa editoryal not; 11 PubMed kaydı ve 1 Endocrine Society açıklaması olmak üzere 12 kaynak.
60 bağımsız deney olduğu iddia edilmez. Aynı yayının bulgusu, yöntem bilgisi ve kanıt sınırından ayrı kartlar üretildi.
Her kartta kaynak kimliği ve yaş aralığı; her kaynakta çalışma türü, katılımcılar, özet, sınır ve bağlantı bulunur. PMID olmayan kurumsal sayfaya sahte PMID verilmedi.
Yaşa göre filtre nedeniyle her kullanıcı 60 kartın tamamını görmez. Yetişkinlerde dışarıdan GH verilmesini inceleyen notlar 18 yaş altına gösterilmez.
“Sprint boyu %135 artırır”, “süt kesin boy uzatır”, genital güneşlenme görevi veya hormon kullanımı önerisi eklenmedi.
Akut hormon yanıtı ile uzun dönem boy değişimi; ilişki ile nedensellik; yetişkin deneyi ile ergen programı ayrı tutuldu.
Kaynakların tamamı 60-BILGI-NOTU.md ve science-cards.json içinde.

## 8. Sesli koç
Cihazdaki çevrimdışı Türkçe metin okuma sesi, hareket başlangıcında isim ve kısa yönergeyi okur. Tekrar dinleme düğmesi vardır.
Türkçe çevrimdışı ses yoksa yazılı rehber devam eder ve neden açıklanır. Uygulama ses modeli indirmez.
Süre sesi: kapalı / son 3 saniye ve bitiş / her saniye. Varsayılan son 3 saniyedir.
Konuşma süre sesine önceliklidir. Arka plana geçiş, ses odağı kaybı veya kulaklık çıkarılması seansı duraklatır.
Mikrofon, ses kaydı veya ağ erişimi izni eklenmedi.
Telefonun ses motoru, ses seviyesi ve Android ses odağı davranışı gerçek cihazda doğrulanmalıdır. Her cihazda ses çalışması garanti edilmez.

## 9. Google Play yayın öncesi kontrolü
Bu bir mağaza onayı değil, riskleri azaltan ürün ve metin düzenlemesidir.
- Sağlık uygulaması beyanı, herkese açık gizlilik politikası ve Data Safety formu tamamlanmalı.
- 13–17 yaş kitlesi ve yaş derecelendirmesi doğru bildirilmeli; çocuklara ilişkin uygulanabilir kurallar ayrıca kontrol edilmeli.
- Uygulama tıbbi cihaz olmadığını ve tanı/tedavi yapmadığını açıklar.
- Gerçek Pro entegrasyonunda fiyat, deneme sonrası ücret, yenileme ve iptal koşulları satın almadan önce gösterilmeli. Firebase tek başına abonelik altyapısı değildir; Play Billing ve sunucu doğrulaması ayrıca gerekir.
- Mağaza metni ve görsellerinde kanıtlanmamış boy/GH vaadi kullanılmamalı.
- Yayından önce güncel politikalar yeniden okunmalı.

İncelenen resmi politika: https://support.google.com/googleplay/android-developer/answer/16679511?hl=en
Ses altyapısı: https://developer.android.com/reference/android/speech/tts/TextToSpeech
Ses odağı: https://developer.android.com/media/optimize/audio-focus

## 10. Doğrulama ve teslim sınırları
Bu sürüm için APK/AAB üretilmedi, Gradle çalıştırılmadı, emülatör açılmadı. Uzak bir sanal ortamda çalıştırılmış gibi sunulmuyor.
Hafif kaynak kontrolleri: JSON yapısı, 60 benzersiz kart, kaynak eşleşmeleri, yaş aralıkları, XML okunabilirliği, eski bitmap referansı bulunmaması, zorunlu seçenekler ve 3 günlük deneme kodu.
28 JUnit test tanımı pakette bulunur (10 yeni profil/çizgi/ses testi, önceki 18 test güncellendi/korundu). Bu sürümde testler çalıştırılmadı.
GitHub Actions için yalnız elle başlatılan derleme/lint/birim test iş akışı eklendi; APK paketleme adımı yoktur. Dosya eklenmesi uzakta çalıştırıldığı anlamına gelmez; kullanıcı kendi deposuna koyup başlatmalıdır.
Henüz yapılmayanlar: Android derleyici doğrulaması, küçük/büyük yazı ekran kontrolleri, TalkBack, fiziksel TTS/kulaklık/arama testi, bildirimlerin farklı pil tasarrufu ayarlarında testi.
Önceki 0.4 test veya APK sonuçları yeni 0.5 sürümü doğrulanmış gibi kullanılmadı.
