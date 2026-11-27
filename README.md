# BERKAH POS

Aplikasi Basic POS Android untuk UMKM Indonesia, dikembangkan oleh **BERKAH TIGA PUTERI**.

## Status
Fondasi Android tahap pertama sedang dibangun pada branch `feat/basic-pos-mvp`. Fitur kasir, master produk lengkap, transaksi, backup/restore JSON, dan promosi SaaS belum dinyatakan selesai.

## Keputusan teknis
- Kotlin + Jetpack Compose + Material 3.
- MVVM, Coroutines, Flow, Navigation Compose.
- Room/SQLite sebagai sumber data POS lokal.
- DataStore untuk preferensi.
- Repository Pattern untuk memisahkan UI dari storage.
- Semua uang disimpan sebagai integer Rupiah (`Long`), bukan Float/Double.
- Tidak ada akun, backend, Firebase, analytics, crash reporting, iklan pihak ketiga, atau permission jaringan pada fondasi.
- Android minSdk 24, compileSdk 37, targetSdk 36.
- AGP 9.6.0, Gradle 9.8.2, Kotlin 2.4.20, JDK 17.
- GitHub Actions menggunakan standard GitHub-hosted runner `ubuntu-24.04`.

## Identitas aplikasi
Candidate applicationId: `id.berkahtigaputeri.basicpos`. Periksa keunikan dan konfirmasi sebelum rilis pertama; jangan mengubah applicationId setelah aplikasi dipublikasikan.

## Branch
- `main`: baseline stabil.
- `feat/basic-pos-mvp`: implementasi aktif.

## Build cloud
Workflow berada di `.github/workflows/android-ci.yml`. CI membangun debug dan menjalankan unit tests/lint. Release AAB hanya dijalankan manual dengan GitHub Environment `android-release` dan signing secrets tersedia.

Untuk mem-bootstrap wrapper pada GitHub-hosted runner, workflow memasang Gradle 9.8.2 lalu menjalankan `gradle wrapper` sebelum `./gradlew`.

## Dokumentasi
- [Arsitektur](docs/ARSITEKTUR.md)
- [Privacy Policy draft](docs/PRIVACY_POLICY.md)
- [Terms of Service draft](docs/TERMS_OF_SERVICE.md)
- [Play Store checklist](docs/PLAY_STORE_CHECKLIST.md)
- [Release process](docs/RELEASE_PROCESS.md)

## Catatan repository public
Repository ini sengaja public agar penggunaan standard GitHub-hosted runners pada public repository tidak dikenakan biaya per menit. Seluruh source code dan konfigurasi non-secret bersifat publik. Jangan memasukkan signing key, password, token, atau data usaha nyata ke Git.
