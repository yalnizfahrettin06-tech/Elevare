# Elevare 0.8 — Ana plan uygulama ve teslim raporu

8 Eylül 2026 · Kaynak kodu teslimi · Ana plan v1.0

## 1. Sonuç ve sınırlar

Bu güncelleme, önceki sürümü yeniden renklendirmekle sınırlı değildir. Onboarding, günün programı, 90 günlük dönemleme, seans kaydı, hareket geometrisi, ses ve bilgi/bildirim akışları ortak kurallarla bağlandı. Kullanıcının istediği ana yapı korunur: koyu ama okunabilir spor arayüzü, basit çizgi adam, anlaşılır bir başlangıç eylemi ve az miktarda kaynaklı bilgi.

Bu teslim **kaynak kodudur**. APK/AAB oluşturulmadı; GitHub'a gönderim, workflow çalıştırma, mağaza yayını, ücretli hizmet veya Firebase kurulumu yapılmadı. Mevcut dosyalar ve eski kullanıcı kayıtları korunacak şekilde sürüm geçişi eklendi.

“Kodlandı”, “otomatik testten geçti”, “gerçek cihazda doğrulandı” ve “uzman tarafından onaylandı” farklı statülerdir. Aşağıdaki bölümler bu ayrımı korur. Yeni egzersiz dozları için uzman onayı veya kaynak editörü kimliği uydurulmadı.

## 2. Sabit kalan kullanıcı kararları

- Koyu gece mavisi zemin; beyaz tema seçeneği yok. Kobalt ana eylem, açık mavi yardımcı vurgu, kontrollü mercan hareket vurgusu.
- Fotoğraf veya gerçekçi insan yerine kodla çizilen vektör çizgi adam. Eski bitmapler aktif arayüzde kullanılmaz.
- 13–21 yaş, zorunlu 11 soru, 5/10/15/30 dakika, haftada 2/3/4 gün.
- Güncel/hedef boy formu yok; kemik görüntüsünden büyüme plağı veya hormon teşhisi yok.
- İlk hazırlık ön planda toplam 30 saniye; tek sonuç ekranı ve tek 3 günlük demo başlangıcı.
- Ana ekranın birincil işi antrenmanı başlatmak/devam ettirmek; bir kısa bilgi kartı, uyku ve program bağlantıları ikinci planda.
- Koşu öncelikli dönemleme; dinlenme, hazırlık, güç ve yoga günleri ayrı. Günlük veya kesintisiz uzun sprint zorlaması yok.
- Sesli koç ve son saniye uyarıları; çevrimdışı kullanım ve düşük donanım gözetilir.

## 3. Ana plan izlenebilirlik tablosu

| Plan bölümü | Entegrasyon karşılığı | Statü / sınır |
|---|---|---|
| 1–3: kapsam, mevcut durum, kimlik | Sürüm 0.8, koyu spor kimliği, yaş/amaç dalları, kullanıcı kararlarının korunması | Kodlandı; mağaza dili ayrı inceleme konusu |
| 4: bilgi mimarisi | Bugün / Antrenman / Bilgi / Profil; eski ana ekranlara rota verilmez | Kodlandı |
| 5: tasarım sistemi | Mevcut koyu renkler korunur; kısa metin, 48+ dp ana etkileşimler, ölçeklenen alanlar | Kaynakta uygulandı; küçük ekran/TalkBack ölçümü cihaz kapısı |
| 6: onboarding | 11 zorunlu yanıt, kayıtlı taslak, geri dönüş, ekipmanda “yok” ayrıklığı, eksik eski alanları tamamlama | Birim testleri var |
| 7: hazırlık/sonuç/demo | Ön plan süresi 30 sn, açıklanabilir aşamalar, gerçek şablon üretimi, tek 72 saatlik demo | Birim testleri var; sahte tıbbi analiz yok |
| 8: ana ekran | Devam et, tamamlandı, dinlenme, plan duraklatıldı, uygunluk engeli, dönem sonu ve günlük kontrol | Kodlandı; gerçek Android görünürlük testi bekliyor |
| 9: program/kütüphane | Bu hafta, 90 gün, 13 hafta, 5 faz, kategori filtreleri, hareket rehberi | Kodlandı; gelecek gün önizlemesi başlatma yetkisi vermez |
| 10: oynatıcı | Kesin seans anlık görüntüsü, kalan/sonraki bölüm, otomatik/manüel geçiş, geri bildirim, kısmi çıkış | Kaynak kontrolü + saf mantık testleri; cihaz sesi bekliyor |
| 11: çizgi adam | Sürekli koşu, yürüyüş temas fazı, yoga giriş–bekleme–çıkış, sağ/sol, yavaş gösterim | Geometri testleri ve gerçek karelerden görsel inceleme yapıldı; uzman/cihaz videosu ayrı |
| 12: katalog | 14 çekirdek hareket; destek/ekipman doğruluğu; gereksiz tekrar ve uygunsuz hareket yerine alternatif | Kodlandı; sonraki hareket adayları eklenmedi |
| 13: süre şablonları | P, R1/R2/R3, G-A/G-B, Y, K, D, N; ısınma/ana blok/geçiş/dinlenme/soğuma hesabı | Kesin bütçe testleri; yeni protokoller inceleme kapılı |
| 14: kişiselleştirme | Uygunluk, ortam, ekipman, süre, spor yükü, yakın seans ve tercih öncelikleri | Motor ve ekranlar aynı kararı kullanır |
| 15: haftalık dağılım | 2/3/4 gün, korunan göreli yerleşim; ilk antrenman tarihini seçme | Serbest tek tek hafta günü sürükleme bu sürüme alınmadı; aşağıda gerekçe var |
| 16–17: 90 gün | Beş faz, hafif haftalar, gün 1–90, son gün özeti, 26/39/51 ana seans takvimi | Takvim ve seviye birbirinden ayrıldı; otomatik seviye artışı yok |
| 18: devamlılık/dönüş | Kısmi kayıt, eksik günleri borçlandırmama, ara günleri, dönüş uyarlaması, yeni döngü arşivi | Birim testleri; geçmiş seans verisi korunur |
| 19: bilim | Mevcut 60 kart / 12 kaynak, yaş/inceleme durumu filtresi, gün içinde kararlılık, tekrar azaltma, kaydetme | Yeni iddia yazılmadı; 60 kartın önerilen yeni konu dağılımı editoryal kapıda |
| 20: uyku/nefes/bildirim | Uyku günlüğü, ayrı isteğe bağlı bildirimler, sessiz saat, günlük tekrar ve kanal çakışması kontrolü | Yerel uygulama; OEM/izin/zaman dilimi cihaz testleri bekliyor |
| 21: profil/ilerleme | Tam/kısmi seans ayrımı, arşiv özeti, yanıt düzenleme, taslak silme, ayarlar | Geçmişi başarı puanı veya hormon skoru gibi sunmaz |
| 22: demo/mağaza | Bir defalık yerel 3 gün/72 saat; ödeme ve yenileme yok | Gerçek abonelik, fiyat, yasal metin ve mağaza beyanları sonraki iş |
| 23: veri/gizlilik | Şema 8, eski sürüm geçişi, duraklatılmış seans kurtarma, bozuk veriyi koruma, dışa aktarım/silme | Birim testleri; bulut/izleme/mikrofon yok |
| 24: teknik ayrıştırma | ProgramCycle, StateCodec, SessionLogic, RuntimeSession, ScienceLogic, ReminderLogic, AudioTiming | Sorumluluklar ayrıldı; komple yeniden yazım yapılmadı |
| 25: erişilebilirlik/donanım | Azaltılmış hareket, sistem hareket tercihi, arka planda durma, tek dinamik figür, vektör varlıklar | Performans hedefleri ölçüm yapılmış gibi sunulmaz |
| 26: test/kabul | Saf Kotlin testleri, kaynak derlemesi, statik denetim, Android test senaryoları | Çalıştırılanlar ve bekleyenler bölüm 10'da |
| 27: teslim kapıları | Kaynak, bu rapor, hareket önizlemesi, tekrar çalıştırılabilir denetimler | Uzman/pilot/yayın kapıları açıkça ayrı |
| 28–30: riskler/dil/kaynaklar | Genç kullanıcıların beden algısını hedefleyen sahte oranlar yok; kaynak ve sınırlamalar korunur | Açık ticari/klinik kararlar kullanıcı adına alınmadı |

## 4. Onboarding akışı

1. Karşılama: tek başlangıç eylemi, kısa ürün açıklaması.
2. Yaş: 13–21.
3. Öncelik: güç/kondisyon, toparlanma/rutin, uyku/enerji veya büyüme hakkında bilgi.
4. Genel hareket alışkanlığı.
5. Koşu deneyimi: genel aktiviteden ayrı.
6. Ortam.
7. Ekipman: sabit sandalye, duvar, yumuşak zemin veya hiçbiri; çelişkili seçim temizlenir.
8. Haftalık gün sayısı: 2/3/4.
9. Günlük süre: 5/10/15/30.
10. Uyku alışkanlığı.
11. Son dönem büyüme gözlemi: belirsiz/belirtmek istemiyorum seçenekleri geçerli yanıttır; hormon hesabı yapılmaz.
12. Uygunluk: ağrı/kısıt veya belirsizlik, antrenman başlangıcını etkiler.
13. Hazırlık: 30 saniyelik görünür düzenleme akışı; arka planda ilerlemez, kapanınca taslak sürer.
14. Sonuç: haftanın gerçek şablon önizlemesi, düzenleme ve tek demo eylemi. Uygunluk engelinde demo pazarlaması yerine programı inceleme.

Soru numarası 11'dir; karşılama, hazırlık ve sonuç soru sayısına dahil değildir. Zorunlu olmak, her soruya tıbbi kesinlikte yanıt vermek zorunda olmak anlamına gelmez. “Bilmiyorum/belirtmek istemiyorum” geçerli bir tercihtir.

Form yanıtları kaydedilmeden sonraki aşama başarıyla tamamlanmış gösterilmez. Eski profil düzenlenirken mevcut döngü, geçmiş, yarım seans ve demo başlangıcı korunur. Düzenleme, ilk kurulumdaki 30 saniyeyi yeniden dayatmaz.

## 5. Ana ekran ve program davranışı

Normal günde seans adı, gerçek toplam süre ve tek “Antrenmana başla” eylemi görünür. Sprint açıklaması rehbere gider; antrenman başlamaz. Günlük kısa kontrol; hazır/yorgun/rahatsızlık, o günkü ortam, yakın yoğun spor ve gerekli alan/destek doğrulamasını içerir. Yanıt seansı değiştirirse başlanacak alternatif adı ve süre önce gösterilir.

Bir ana seans bitince aynı gün ikinci ana seans zorlanmaz. Yarım seans açıkken yeni seans açmak yerine mevcut seansa dönülür. İlerlemede tamamlanmış ve kısmi kayıtlar farklıdır. Kısmi kayıt tam seans veya ilerleme hakkı olarak sayılmaz.

Takvimde gelecekteki şablon adayları görülebilir; geleceğin planına tıklamak bugünkü uygunluk ve inceleme sınırlarını aşmaz. Geçmişte tamamlanan seansın adı/süresi yeni tercihlerle yeniden yazılmaz. 90. gün özeti kendiliğinden ikinci döngü veya yeni demo başlatmaz.

### Kontrollü küçük kapsam uyarlaması

Ana planın 15.3 bölümündeki serbest hafta günü yerleşimi, ilk aşamada ilk antrenman tarihini seçme ve denenmiş 2/3/4 günlük göreli yerleşimle sınırlandırıldı. Tek tek günleri taşıma, sonraki haftanın sınırında koşu aralığını da doğrulayan ayrı bir yerleşim arayüzü gerektirir. Mevcut sürüm bunu varmış gibi göstermez veya güvensiz bir yerleşimi sessizce kabul etmez. Kullanıcı istediğinde bu ayrı deneyim sonraki küçük sürümde genişletilebilir.

İlk tarih bugün ile sonraki altı gün arasından, ana seans kaydı oluşmadan seçilebilir. Başlangıç ilerideyse ana ekran ve takvim “bugün başladı” demez; erken ana antrenman ve antrenman bildirimi engellenir. Mevcut tam/kısmi geçmişi olan döngünün başlangıcı bu ayardan geriye dönük taşınmaz.

## 6. Program motoru ve uzman kapısı

ProgramCycle tek karar kaynağıdır. Haftalık çizelgeyi 13 kez kopyalamak yerine faz, gün, tercih, ekipman, geri bildirim ve yük durumuna göre günleri çözer. 90 gün boyunca takvim ilerlemesi ayrı, egzersiz seviyesi ayrıdır.

- 2/3/4 gün kapasitesi: standart döngüde 26/39/51 ana seans; son gün dönem özeti.
- 5 dakika seçimi koşu intervalini zorla sığdırmaz; hazırlık P'dir.
- P ve güç şablonlarının toplamları geçiş/dinlenme dahil hesaplanır.
- Koşu toplamında ısınma ve soğuma vardır. “10 dakika” kesintisiz 10 dakika sprint değildir.
- K seansında koşu ve desteklerin aynı güvenli alanda olduğu o gün doğrulanmalıdır.
- Yoga 30 dakika seçildi diye sınırlı hareketleri yapay biçimde uzatmaz; kısa alternatifin gerçek süresi gösterilir.
- Yakın koşu yükü tarih değil mümkün olduğunda gerçek zamanla kontrol edilir. Eski tarih-only kayıtlarda daha koruyucu dönüş kullanılır.
- Ağrı, zorlanma, ara ve dış spor yükü daha kolay alternatif veya durma sonucuna gidebilir.

`RELEASE_PROGRAM_APPROVALS` içindeki koşu, güç, yoga ve kombine onayları **kapalıdır**. Bu, motorun eksik olduğu anlamına gelmez; yeni dozların inceleme beklediğini uygulamanın gerçekten uygulamasıdır. Takvim adayını incelemek mümkün; yürütülen hazırlık alternatifi ayrı etiketlenir. Onay açmak için yalnız Boolean değiştirmek yeterli süreç sayılmaz: gerçek uzman incelemesi, kayıt/sürüm bilgisi, hareket ve cihaz kanıtı gerekir.

Kullanıcı onayı da genel bir “bundan sonra hep zorlaştır” izni değildir. Seviye önerisi uygun kayıtlara, güncel profile ve inceleme sürümüne bağlanır; her seviye için yeniden doğrulanır. Onay verilmemiş içerik için yükseltme eylemi sunulmaz.

## 7. Çizgi adam, oyuncu ve ses

Figür; dairesel baş, sabit uzunluklu segmentler, eklem koordinatları ve sınırlı renk vurgusundan oluşur. Koşu ile yürüyüş aynı animasyonun hızlandırılmış hali değildir. Koşuda toparlanan bacak, yer teması, karşı kol ve kısa uçuş fazı; yürüyüşte temas korunması ayrı hesaplanır. Yoga için rastgele titreşim yerine giriş/bekleme/çıkış ve taraf bilgisi kullanılır.

`motion-contact-sheet.svg/png` doğrudan uygulamanın `motionFrame` fonksiyonundan alınmıştır. Altı hareketin sekiz karesi görsel olarak incelendi; kesilme veya bariz eklem kopması görülmedi. `motion-preview.html`, aynı üretim verisinden 16 hareket/taraf varyantını normal/yavaş/duraklatılmış oynatır. Bu bir tasarım inceleme aracı; Android ekran kaydı veya klinik teknik onayı değildir.

Oynatıcıda gerçek seans anlık görüntüsü saklanır. Profil değişince yarım seansın adımları değişmez. Süre dolunca manüel ayar hariç sonraki bölüm kendiliğinden açılır. Arka plan, ses odağı kaybı ve kulaklık çıkması duraklatmayı tetikler. Kayıt başarısız olursa başarı mesajı gösterilmez; ekran dışındaki seans disk hatası yüzünden çalışmaya devam etmez.

Türkçe ses cihazda çevrimdışı bulunabiliyorsa kullanılır; yoksa metinli koç ile devam edilir. Mikrofon izni istenmez. Kullanıcı sesli anlatımı ve sayaç sesini ayrı ayarlayabilir. Gerçek telefonun Türkçe TTS paketi, Bluetooth/ses odağı ve bildirim davranışı bu teslimde cihazda denenmedi.

## 8. Bilgi, bildirim ve veri

Mevcut 60 kart korunur. Kaynaklar, çalışma grubu, bulgu ve sınırlar ayrıdır. Yeni ve doğrulanmamış “boyu yüzde X artırır” gibi kesin pazarlama iddiaları eklenmedi. Kaydetme, aynı gün kart kararlılığı, son günlerde görüleni azaltma ve kaynak çeşitliliği mantığı eklendi. Eski kartlar “mevcut kaynak özeti” olarak ele alınır; yeni uzman incelemesinden geçmiş gibi işaretlenmez.

Uyku ve antrenman hatırlatıcıları ayrıdır; kullanıcı açmadan etkinleşmez. Antrenman tamamlanmışsa, seans açıksa, plan duraklatılmışsa, dinlenme günüyse veya uygunluk engeli varsa antrenman hatırlatıcısı gönderilmez. Sessiz saat, aynı kanalda günlük tekrar ve iki kanalın yakın çakışması denetlenir. Yeniden başlatma ve saat/zaman dilimi değişimi alarmları yeniden kurar. Bildirim tıklaması uygun sayfaya yönlenir.

Uyku bildirimi yatmadan 15/30/60 dakika önce seçilebilir; varsayılan 30 dakikadır. Profil ve uyku ekranı aynı seçimi gösterir; saat, uyanma hedefi veya erken uyarı aralığı değişince alarm yeniden planlanır. Çok geç teslim edilen alarm yatma saatinden sonra kullanıcıyı uyandıracak biçimde gösterilmez.

Yeni kayıt şeması; taslak, döngü kimliği, arşiv, seans anlık görüntüsü, kısmi/tam durum, kaynak tercihleri ve demo başlangıcını saklar. Eski `p7` seans tarifleri çözülmeye devam eder. Eski boy alanları yeni şemaya taşınmaz. Bozuk/gelecek şema verisi sessizce boş profile çevrilip üzerine yazılmaz; kurtarma dışa aktarımı sunulur. Dosya sağlayıcısı yazmayı reddederse yanlış başarı bildirimi verilmez. Silme onayı başarılı olduğunda alarmlar ve yerel bildirim durumları da temizlenir.

## 9. Özellikle değiştirilmeyenler

- Ana planın orijinal metni değiştirilmedi.
- Kullanıcının eski bitmap varlıkları silinmedi; aktif kaynak paketi için gereksizdir.
- Yeni sağlık iddiası, hormon/kemik teşhisi, görünüş puanı ve boy garantisi yok.
- Kimlik doğrulama, Firebase, gerçek abonelik, satın alma veya kamuya açık yükleme yok.
- Önceki kaynak raporları korunur; eski ekran fonksiyonları referans/uyumluluk için kalabilir fakat ana navigasyonda kullanılmaz.

## 10. Doğrulama kaydı

| Kontrol | Gerçek sonuç |
|---|---|
| Saf Kotlin/JUnit | **121 test geçti**, 8 test sınıfı, JUnit çıkışı 0 |
| Son ana kaynak derlemesi | **29 Kotlin dosyası**, Kotlin/Compose 2.0.0, 43 önbellek bağımlılığı; çıkış 0 |
| Kaynak sabitliği | Derleme öncesi/sonrası SHA-256 ve dosya sayısı eşleşti |
| Statik kaynak denetimi | Geçti: 60 kart, 12 kaynak, referans/yaş/PMID eşleşmeleri, zorunlu form, 30 sn, tek 3 gün, XML ve izinler |
| Hareket dışa aktarımı | 16 varyant × 120 kare = **1.920 üretim karesi**; JSON/SVG geçerli, önizleme JavaScript sözdizimi geçerli |
| Görsel inceleme | Altı hareket × sekiz kare kontakt sayfası incelendi; Android görüntüsü yerine üretim geometri incelemesi |
| Fark ve betik denetimi | `git diff --check`, cihaz kontrolü betiğinin sözdizimi ve teslim betiği sözdizimi geçti |
| APK/AAB/kurulum | **Çalıştırılmadı** |
| Android instrumentation / gerçek telefon | **Çalıştırılmadı** |
| Uzman teknik/klinik onayı | **Alınmadı; ilgili kapılar kapalı** |

Kullanılan hafif yerel kontroller: saf mantık derlemesinde 256 MB, test JVM'inde 128 MB; ana kaynak derlemesinde 384 MB bellek üst sınırı. JVM işlemleri sırayla yürütüldü. Ana derleyicide yalnız eski simge ve sistem çubuğu API'lerine ilişkin kullanım dışı uyarıları kaldı; derleme hatası yok.

Son kayıtlar teslim paketinin `verification/` klasöründedir. ZIP içindeki `SHA256SUMS.txt`, her teslim dosyasının özetini içerir; paket hazırlanırken arşivden geri okunup kaynak dosyalarla karşılaştırılır. Kontrol kavramları birbirinin yerine kullanılmaz: kaynak dosyası testten geçmesi, telefon videosunun veya klinik onayın yerine geçmez.

Çalıştırılan kontrollerin sınırlaması: Kotlin/Compose kaynak derlemesi, Android kaynak paketleme/dex/imza/kurulum zincirinin tamamı değildir. Saf JUnit testleri gerçek telefonun yaşam döngüsü, görüntü, ekran okuyucu veya ses donanımını taklit ederek doğruladığımız anlamına gelmez.

Android test senaryoları ve CI kanıt toplama betiği güncellendi. Test bağımlılıkları yerel önbellekte olmadığından `androidTest` bu bilgisayarda derlenmedi/çalıştırılmadı. Lokal Gradle veya emülatör açılmadı. Gelecek yetkili CI çalışması gerçek ekran görüntüleri ile normal/yavaş koşu ve yoga video kanıtını üretmelidir; eksik kanıt CI sonucunu başarısız yapar.

## 11. Yayın öncesi kalan kapılar

1. Yeni egzersiz dozları ve özellikle genç kullanıcı dalları için gerçek uzman incelemesi.
2. Android üzerinde 360 dp ve büyük yazı, TalkBack, arka plan/işlem ölümü, ses odağı, Türkçe TTS, bildirim izinleri ve OEM pil kısıtları.
3. Gerçek animasyon videolarının kullanıcı tarafından görsel kabulü; hareket formunun uzman doğrulaması.
4. 60 kart için önerilen yeni konu dağılımı ve editoryal kayıtların tamamlanması; yeni iddiaların ayrı kaynak kontrolü.
5. Serbest hafta günü yerleşimini genişletme kararı.
6. Abonelik/mağaza/Firebase ayrı kapsamı ve gerekli ticari/yasal kararlar.

## 12. Geliştirici için başlangıç

- Ana akış: `MainActivity.kt`, `OnboardingV5.kt`, `WorkoutHome.kt`.
- Program: `TrainingProfile.kt`, `ProgramCycle.kt`, `WorkoutProgram.kt`.
- Seans: `SessionLogic.kt`, `RuntimeSession.kt`, `TrainingScreens.kt`, `Store.kt`, `StateCodec.kt`.
- Görsel/ses: `StickGeometry.kt`, `WorkoutUI.kt`, `CoachAudio.kt`, `AudioTiming.kt`.
- Bilgi/bildirim: `ScienceLogic.kt`, `ScienceCards.kt`, `ReminderLogic.kt`, `Reminder.kt`.
- Doğrulama: `app/src/test`, `app/src/androidTest`, `scripts/verify-source.ps1`, `scripts/device-qa.sh`.

Zamanlayıcının duvar saatinden bağımsız çalışma kararı Android'in [SystemClock belgesindeki](https://developer.android.com/reference/android/os/SystemClock) monoton saat ayrımına dayanır. Bilim alanında mevcut kaynakların sınırları korunmuştur; örneğin akut hormon yanıtı araştırmasını boy artışı garantisine dönüştürmemek için [PMID 12137178](https://pubmed.ncbi.nlm.nih.gov/12137178/) gibi çalışmalarda ölçülen sonuç ile kullanıcıya yapılan iddia ayrı tutulur.
