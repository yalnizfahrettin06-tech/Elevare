# Elevare 0.6 — Kaynak kod
Yeni çizgi figür ve akış iyileştirmeleri. APK/AAB içermez.
Önce docs/DEGISIKLIKLER-0.6.md dosyasını okuyun.
Sprint çizimine Antrenman > Sprint çizim rehberi yoluyla ulaşılır. Rehber süreli sprint programı başlatmaz.
Onboarding'in zorunlu cevapları, 13–21 yaş, 5/10/15/30 dakika, 45 saniye ve sonunda 3 günlük demo korunmuştur.
Ana ekran tamamlanan seansı ve öneri nedenini gösterir.

## Kontroller
scripts/verify-source.ps1 Android araçlarını başlatmadan kaynakları kontrol eder.
scripts/GeometryCheck.kt gerçek geometri/model/profil koduyla birlikte çalışan bağımsız Kotlin kontrolüdür.
Bu teslimde geometri/model/profil derlemesi ve kontrolü yapıldı; tam Android/Compose derlemesi ve cihaz testi yapılmadı.
.github/workflows/source-check.yml kullanıcının kendi deposunda elle başlatabileceği Android Kotlin/JUnit/lint iş akışıdır. Bu teslim sırasında uzak sunucuya kaynak yüklenmedi.
Güncel değişiklikler docs/DEGISIKLIKLER-0.6.md, çalıştırılan kontroller docs/KONTROL-SONUCU-0.6.txt dosyasındadır.
