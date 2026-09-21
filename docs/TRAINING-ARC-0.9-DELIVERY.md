# Elevare 0.9 · Training Arc

## Tasarım kararı

Son istekteki **premium Training Arc** yönü esas alındı. Önceki mavi spor arayüzü ve daha eski “premium olmasın” tercihi bu görsel yön için geçerli değil. Antrenman, süre, yaş ve güvenlik kararları ise değiştirilmedi.

Training Arc burada bir savaşçı kostümü, anime lisansı veya hormon seviyesi oyunu değil: kişinin kendi antrenman hikâyesi. Bölümler mevcut 90 günlük programın beş fazını temsil ediyor. Dönüm noktaları görünüşe, boya veya başkalarıyla rekabete bağlanmıyor.

## Görsel sistem

| Rol | Değer | Kullanım |
|---|---|---|
| Sıcak kömür | `#151414` | Ana zemin ve sistem çubukları |
| Sahne | `#211D1C` | Antrenman sahnesi |
| Mercan | `#F28B74` | Ana eylem, seçili durum, atlet imzası |
| Sıcak açık | `#F4EFE8` | Ana yazı ve figür |
| Şeftali | `#E8B9A5` | İkincil hareket vurgusu |
| İkincil yazı | `#BDB3AB` | Kısa açıklamalar |
| Sınır | `#4B403C` | İnce ayırıcı ve sahne kenarı |

- Mercan düğmelerde koyu yazı; beyaz yazılı parlak mercan düğme kullanılmaz.
- Doygunluk tüm ekranı kaplamaz. Sıcak koyu yüzeyler, boşluk ve vurgu dengesi premium hissini taşır.
- Büyük sloganların yanında küçük bölüm etiketleri; bütün başlıklar büyük harfe zorlanmaz.
- Asimetrik köşe, bölüm çizgileri ve yay biçimi ortak marka işaretidir.
- Ağdan font/ikon/görsel çekilmez. Sistem sans-serif ve özgün 24 birim çizgi ikonları kullanılır.
- Uygulama açılış simgesi de yeni Arc işaretine geçirildi.

## Onboarding

1. Karşılama: “Hikâyen hareketle başlar.”, hareketli çizgi atlet, plan ve sesli koç özeti.
2. Mevcut zorunlu 11 soru korunur. Yaş 13–21; süre 5/10/15/30 dakika; haftada 2/3/4 gün.
3. İnce 11 parçalı ilerleme şeridi, küçük soru kategorisi ikonu, belirgin seçili yanıt yüzeyi.
4. Tek soru / ekran; uzun içerik kaydırılabilir, devam düğmesi altta kalır.
5. 30 saniyelik mevcut hazırlık akışına yeni atlet sahnesi ve ilerleme sunumu uygulanır.
6. Sonuç: “İlk bölümün hazır.”; gerçek seçilmiş gün/süre bilgileri ve mevcut program önizlemesi.
7. Mevcut tek 3 günlük demo eylemi korunur. Ücret, abonelik ve otomatik yenileme başlamaz.

Form taslağı, geri gezinme, zorunlu yanıt denetimi, kayıt hatası ve düzenleme akışı değiştirilmedi. Arayüz yenilendi diye mevcut kullanıcının onboarding'i veya denemesi sıfırlanmaz.

## Ana ekran ve program

- Marka başlığı ve gerçek program fazına bağlı bölüm şeridi.
- Ana antrenman kartı: çizgi atlet sahnesi → antrenman adı → süre/hareket bilgisi → tek baskın başlat eylemi.
- Isınma/toparlanma bilgisi ve hareket rehberi daha düşük görsel öncelikte.
- Günlük bilgi kartı “Saha notu” olarak sunulur; kaynak ve ayrıntı içeriği korunur.
- Alt gezinme: Bugün / Program / Notlar / Profil. Sayfa yönlendirme mantığı aynı.
- Dinlenme, tamamlanma, ara verme, planın başlamaması, kısmi oturum ve güvenlik durumları ayrı kalır.
- Bölüm şeridi mevcut faz başlangıçları olan 1, 15, 29, 50 ve 78. günlerle eşleşir; ikinci bir ilerleme motoru yoktur.

## Çizgi atlet

- Basit baş halkası, çizgi gövde ve uzuvlar korunur; gerçekçi yüz, kas çizimi, fotoğraf veya kostüm eklenmez.
- Başta kısa mercan yay, figürün ayırt edici küçük imzasıdır.
- Öndeki ve arkadaki uzuvlar farklı tonlarla okunur; boyun/kalça birleşimleri temizlenir.
- Statik yay ve pist çizgileri hareketin önüne geçmeyen bir sahne sağlar.
- Aynı üretim geometrisi kullanılır: bu güncelleme koşu tekniğini yeniden tasarladığı veya biyomekanik onay aldığı iddiasında değildir.
- Hareket saati Canvas'ın çizim aşamasında okunur; her karede tüm çevre arayüzünü yeniden kurma ihtiyacı azaltılır. Cihaz performansı ölçülmeden FPS iddiası yapılmaz.
- Ön plan/arka plan, duraklatma, yavaş gösterim, sistem animasyon tercihi ve azaltılmış hareket davranışları korunur.

Hareket inceleme HTML/SVG çıktısı üretimdeki `motionFrame` eklem verisinden alınır. Android ekran görüntüsü değildir; sahne, metin düzeni ve gerçek cihaz akıcılığını kanıtlamaz.

## Diğer yüzeyler

Profil, program satırları, hareket detayları, antrenman oynatıcısı, sonuç, bilgi kütüphanesi, uyku ve geçmiş ekranları aynı palet ve çizgi ikon ailesine geçirildi. Nefes halkasının vurgusu mercan yapıldı. Profilde “Senin ritmin.” başlığı ve Training Arc görünüm açıklaması bulunur.

Ortak düğmeler en az 56 dp, ikon düğmeleri Material dokunma alanlarını kullanır. Dekoratif çizimler erişilebilirlik ağacından ayrılır. Hareketin adı/yönergesi erişilebilir açıklama olarak kalır. Ana kart ve karşılama metaverileri büyük yazı boyutunda satır değiştirebilir.

## Değişmeyen alanlar

- 90 günlük program kuralları, fazlar, dinlenme günleri ve süre hesabı.
- Yaş/ortam/ekipman/deneyim uyarlaması ve hazırlık uygunluk kontrolleri.
- Uzman incelemesi bekleyen protokollerin kapalı yayın onayları.
- Aktif oturumun anlık görüntüsü, monoton sayaç, duraklatma ve kısmi kayıt.
- Yerel veri şeması, uyku/antrenman hatırlatıcıları ve sesli koç davranışı.
- 60 mevcut bilgi kartı ve kaynakları; yeni hormon/boy artışı yüzdesi yok.
- Firebase, hesap, sensör, ödeme veya yeni izin yok.

## Doğrulama ve sınırlar

Bu turda tamamlananlar:

- 121 JVM testi: başarılı.
- 30 Kotlin dosyasının doğrudan Kotlin/Compose kaynak derlemesi: çıkış 0. Sistem çubuğu API'lerine ait kullanımdan kaldırılma uyarıları var; derleme hatası yok.
- Mevcut statik kaynak kontrolleri: başarılı.
- 23 Training Arc tasarım sözleşmesi kontrolü: başarılı. Ana metin/zemin 16,08:1; ikincil metin/yüzey 7,18:1; düğme metni/mercan 7,64:1 ölçüldü. Bunlar belirtilen renk çiftleridir, tüm ekranların erişilebilirlik sertifikası değildir.
- 16 hareket/yön × 120 poz: 1.920 pozluk üretim-geometrisi çıktısı; koordinatlar sonlu, HTML oynatıcı betiği sözdizimi geçerli.
- `git diff --check`: hata yok.

Tarayıcı kontrol aracı iki kez zaman aşımına uğradığı için hareket HTML'inin bu turdaki görsel incelemesi tamamlanamadı. Bu eksik kontrol, otomatik kaynak testleriyle yapılmış sayılmadı.

Tamamlanan kontrol sonuçları teslim paketindeki `verification` klasöründedir. Statik kontrol, JVM birim testleri ve doğrudan Kotlin/Compose kaynak derlemesi birbirinden farklı kontrollerdir. Kaynak derlemesi mevcut önbellek ve önceden üretilmiş R.jar kullanır; Android kaynak bağlama, dex, APK veya Gradle bağımlılık çözümlemesi değildir.

Cihaz/emülatör ekran testleri, TalkBack, 200% yazı boyutu, ses-hareket senkronu ve gerçek cihaz performansı bu teslimin tamamlanmış doğrulaması değildir. `WorkoutJourneyTest` yeni karşılama ve gezinme etiketleriyle güncellendi; yeniden Android ortamında çalıştırılmalıdır. Bu güncelleme için APK üretilmedi, GitHub'a gönderilmedi ve Actions başlatılmadı.

## Sonraki cihaz kabul listesi

- 360 dp genişlikte ilk ekranda eylemin görünürlüğü; 200% yazı boyutunda seçenek/düğme taşması.
- 11 soruda ileri/geri, seçmeden ilerleyememe, uygulama kapanıp açıldığında taslak.
- Hazırlık süresinde arka plana geçiş; sonuçta tek demo eylemi.
- Ana ekranın dinlenme, hazırlık alternatifi, tamamlanmış ve aktif seans durumları.
- Sprint/yürüyüş farkı, yoga giriş/tutuş/çıkış, duraklatınca figürün durması.
- Azaltılmış hareket ve sistem animasyon kapalıyken sabit gösterim.
- Ekran okuyucu odak sırası, ikon açıklamaları ve bölüm şeridinin tek okunması.
- Gerçek cihazda sesli koç, geri sayım, kulaklık ayrılması ve geri yüklenen seans.

## Başvurulan teknik kaynaklar

- [Android: Compose çizim sistemi](https://developer.android.com/develop/ui/compose/graphics/draw/overview) — Canvas ve çizim aşaması.
- [Android: varsayılan erişilebilirlik davranışları](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) — etkileşim alanları ve kontroller.
- [Android: semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics) — özel çizim ve ikonların açıklamaları.

Bu belgeler tasarımın klinik geçerliliğini veya mağaza onayını sağlamaz.
