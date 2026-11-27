# BERKAH POS

Aplikasi Basic POS offline-first untuk UMKM Indonesia, dikembangkan oleh **BERKAH TIGA PUTERI**.

## Status implementasi sumber

Implementasi MVP POS telah digabungkan ke branch `main` melalui [PR #1](https://github.com/berkah-pos/btp/pull/1) pada 27 November 2026.

Source code saat ini mencakup:
- Dashboard penjualan harian, jumlah transaksi, produk, dan stok rendah yang dibaca dari Room.
- Profil usaha dan preferensi ambang stok rendah melalui DataStore.
- CRUD produk, pencarian nama/SKU, kategori, dan stok.
- Kasir, keranjang, diskon/pajak nominal, pembayaran, kembalian, dan checkout Room atomik.
- Riwayat transaksi, detail item snapshot, dan berbagi struk. Identitas toko juga disimpan per transaksi agar struk historis tidak berubah ketika profil toko diperbarui.
- Backup/restore JSON lokal melalui Android Storage Access Framework.
- Informasi SaaS yang URL HTTPS/email-nya dikonfigurasi melalui GitHub Actions Variables.
- Pilihan data contoh yang eksplisit, hanya katalog produk berlabel `[CONTOH]`, tanpa transaksi palsu.

**Status validasi (27 November 2026):** PR #3 telah digabungkan ke `main` setelah workflow cloud berhasil pada commit PR `b0c9d1988d39209b29cfcaee3f03923a47b834dc`. Run [45969254895](https://github.com/berkah-pos/btp/actions/runs/45969254895) menjalankan `testDebugUnitTest` (16 test, 0 gagal, 0 error, 0 skipped), `lintDebug` (0 error, 8 warning), dan `assembleDebug` (berhasil); artifact APK debug serta laporan lint/test berhasil diunggah. Delapan warning lint mencakup target API yang belum setinggi compile SDK, versi Gradle/AGP/dependency yang memiliki rilis lebih baru, konfigurasi backup Android 12+, resource launcher yang tidak dipakai, serta resource shrinking. Warning tersebut bukan error build, tetapi harus ditinjau sebelum rilis. Job signed release AAB dilewati karena run berasal dari pull request; AAB hanya dibuat melalui `workflow_dispatch` pada `main` setelah identitas aplikasi, baseline dependency verification, dan signing secrets tersedia. Belum ada uji startup pada perangkat fisik/emulator, UI automation, internal testing, atau Pre-launch Report. Room schema v2 dan dependency verification metadata telah dihasilkan sebagai artifact CI, tetapi belum menjadi file yang dikomit pada branch kanonis; strict dependency verification baru dianggap aktif setelah metadata tersebut direview dan dikomit.

## Keputusan teknis

- Kotlin + Jetpack Compose + Material 3.
- MVVM, Coroutines, Flow, Navigation Compose.
- Room/SQLite sebagai sumber data POS lokal. Database schema versi 2 memiliki migrasi non-destruktif dari versi 1 untuk mempertahankan transaksi yang sudah tersimpan.
- Preferences DataStore untuk ambang stok rendah.
- Repository Pattern untuk memisahkan UI dari storage.
- Semua uang disimpan sebagai integer Rupiah (`Long`), bukan Float/Double.
- Tidak ada akun, backend, Firebase, analytics, crash reporting, iklan pihak ketiga, atau permission jaringan yang dideklarasikan.
- Android minSdk 24, compileSdk 37, targetSdk 36.
- AGP 9.5.1, Gradle 9.8.2, built-in Kotlin 2.4.20, KSP 2.3.14, Coroutines 1.11.0, JDK 17.

## Identitas aplikasi

Candidate applicationId: `id.berkahtigaputeri.basicpos`. Debug build menggunakan kandidat ini. Sebelum signed release, konfirmasi ID permanen melalui Play Console dan isi GitHub Actions Repository Variable `BTP_APPLICATION_ID`; release workflow akan gagal bila belum diisi. Jangan mengubah applicationId setelah publikasi pertama.

## Branch dan review

- `main`: baseline kanonis yang berisi implementasi MVP hasil merge PR #1 dan koreksi CI PR #3.
- Perubahan lanjutan dilakukan pada branch kerja terpisah dan diajukan melalui pull request.
- [PR #1 — Android foundation](https://github.com/berkah-pos/btp/pull/1) telah **merged** menggunakan Squash and merge pada 27 November 2026.
- [PR #3 — perbaikan Android SDK dan CI](https://github.com/berkah-pos/btp/pull/3) telah **merged** setelah quality workflow berhasil pada 27 November 2026.

## Cloud build

Workflow berada di `.github/workflows/android-ci.yml` dan menggunakan GitHub-hosted `ubuntu-24.04`. Workflow dirancang untuk menjalankan unit tests, lint, dan debug APK. Signed release AAB hanya melalui manual `workflow_dispatch` dari `main`, GitHub Environment `android-release`, dan upload-key secrets.

Untuk bootstrap Gradle Wrapper di runner, workflow memasang Gradle 9.8.2 lalu membangkitkan wrapper di workspace runner sebelum menjalankan `./gradlew`. Tidak ada wrapper JAR yang dikomit pada baseline ini.

## Konfigurasi promosi dan dukungan

GitHub Actions Repository Variables opsional:
- `BTP_SAAS_URL`: URL HTTPS resmi layanan SaaS.
- `BTP_SUPPORT_EMAIL`: email dukungan resmi.

Keduanya sengaja kosong secara default. Jangan mengisi alamat placeholder atau informasi perusahaan yang belum diverifikasi.

## Dokumentasi

- [Arsitektur](docs/ARSITEKTUR.md)
- [Kebijakan Privasi — draf](docs/PRIVACY_POLICY.md)
- [Syarat dan Ketentuan — draf](docs/TERMS_OF_SERVICE.md)
- [Google Play checklist](docs/PLAY_STORE_CHECKLIST.md)
- [Release process](docs/RELEASE_PROCESS.md)

## Backup dan privasi

Backup JSON versi 3 berada di lokasi pilihan pengguna dan **tidak dienkripsi oleh aplikasi**. File memuat profil toko, kategori, produk, stok, transaksi, snapshot identitas toko per transaksi, rincian struk, serta preferensi ambang stok rendah. Restore tetap menerima backup versi 2; transaksi lama tanpa snapshot tetap menampilkan identitas toko sebagai tidak tersedia, bukan mengambil profil terkini. Jaga file backup tersebut; jangan menyimpan data usaha nyata pada issue, log, atau commit publik.

## Repository public

Repository ini sengaja public agar standard GitHub-hosted runners pada public repository dapat digunakan tanpa biaya menit runner. Artifact storage tetap mempunyai kuota dan retensi. Jangan commit signing keystore, password, token, atau data usaha nyata.
