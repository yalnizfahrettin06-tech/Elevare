# Elevare 0.5 — kaynak kod
Bu paket yeni Android kaynaklarını içerir. APK, AAB, imza anahtarı, yerel SDK yolu ve derleme önbelleği içermez.
Yeni sürüm derlenmemiştir; Android üzerinde çalıştığı henüz doğrulanmamıştır.

## Başlangıç
Önce docs/UI-UX-RAPORU-0.5.md dosyasını okuyun.
- Yeni onboarding: OnboardingV5.kt / TrainingProfile.kt
- Ana ekran ve demo: GrowthScreens.kt
- Çizgi adam: StickGeometry.kt / TrainingScreens.kt
- Sesli koç: CoachAudio.kt
- Bilgi havuzu: app/src/main/assets/science-cards.json
- Kaynak ve sınırlar: docs/60-BILGI-NOTU.md
- Figür önizlemesi: docs/CIZGI-FIGURLER.svg

## Hafif kontrol
PowerShell ile scripts/verify-source.ps1 çalıştırılabilir. Android SDK veya Gradle başlatmaz.

## İsteğe bağlı uzak doğrulama
.github/workflows/source-check.yml yalnız workflow_dispatch ile başlatılır.
Kendi GitHub deponuza aktarıp Actions üzerinden çalıştırın. Bu teslim sırasında herhangi bir depoya yükleme veya uzak işlem yapılmadı.
İş akışı JDK 17 / Android SDK ile Kotlin derleme, JUnit ve lint çalıştırır; assemble/package veya APK yükleme adımı yoktur.
Yerel bilgisayarda çalıştırmak zorunda değilsiniz. Üçüncü taraf CI kullanımı kendi hesabınızın kota/ücret ve gizlilik koşullarına tabidir.

## Bilinen sınırlar
45 saniyelik ekran şeffaf bir hazırlık gösterimidir, tıbbi analiz değildir.
60 not 12 kaynaktan türetilmiştir; 60 ayrı çalışma değildir.
Günlük 30 dakika seçimi bir zaman bütçesidir, yeni 30 dakikalık yoğun egzersiz programı değildir.
3 günlük Pro ekranı ücret almayan demodur, gerçek abonelik veya süre kilidi yoktur.
Türkçe koç cihazda çevrimdışı Türkçe TTS sesi varsa konuşur.
Eski boy alanları veri uyumluluğu için modelde kalır, yeni onboarding bunları sormaz.
