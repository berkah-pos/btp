# BERKAH POS

Aplikasi Basic POS offline-first untuk UMKM Indonesia, dikembangkan oleh **BERKAH TIGA PUTERI**.

## Status implementasi

Fondasi MVP POS tersedia pada `main` melalui [PR #1](https://github.com/berkah-pos/btp/pull/1). Implementasi mencakup:

- Dashboard penjualan harian, jumlah transaksi, produk, dan stok rendah dari Room.
- Profil usaha dan preferensi ambang stok rendah melalui DataStore.
- CRUD produk, pencarian nama/SKU, kategori, dan stok.
- Kasir, keranjang, diskon/pajak nominal, pembayaran, kembalian, dan checkout Room atomik.
- Riwayat transaksi, detail item snapshot, dan berbagi struk. Identitas toko dan rincian item disimpan sebagai snapshot saat checkout agar struk historis tidak berubah ketika master data diperbarui.
- Backup/restore JSON lokal melalui Android Storage Access Framework.
- Informasi SaaS opsional yang hanya ditampilkan ketika URL HTTPS/email dukungan resmi dikonfigurasi.
- Data contoh eksplisit yang hanya menambahkan katalog produk berlabel `[CONTOH]`, tanpa transaksi palsu.

## Validasi cloud yang teramati

GitHub Actions [run #41](https://github.com/berkah-pos/btp/actions/runs/45989244818) selesai **success** pada percobaan ke-2 tanggal 27 November 2026. Run ini memvalidasi PR #4 pada head `1365bab63bb91032b56bb4279f6dec212574d778`; runner membangun merge commit `e4074059ef147bc64302431c7cee579679d169da` terhadap `main` `45c289590772a136b83a73b528d3651ed8f26aae`.

Hasil yang diverifikasi dari job, log, dan artifact:

- `testDebugUnitTest`: **24 test, 0 failure, 0 error, 0 skipped**, dihitung dari empat JUnit XML.
- `compileDebugAndroidTestKotlin`: **success**; source instrumentation test migrasi Room berhasil dikompilasi, tetapi belum dieksekusi pada emulator.
- `lintDebug`: **0 error, 1 warning** (`OldTargetApi` pada `targetSdk=36`).
- `assembleDebug`: **BUILD SUCCESSFUL**; APK debug berukuran 14.944.217 byte.
- Pemeriksaan APK ZIP alignment 16 KB: **success**. Empat native library arm64-v8a/x86_64 yang dikemas juga lulus pemeriksaan ELF LOAD alignment 16 KiB.
- Merged release manifest dan release runtime dependency inventory berhasil diunggah untuk audit. Manifest hanya mendeklarasikan permission signature milik aplikasi untuk dynamic receiver; tidak ada INTERNET atau permission kontak, telepon, kamera, lokasi, dan broad-storage. Inventory yang ditangkap tidak menunjukkan Firebase, analytics, Crashlytics, SDK iklan, OkHttp, atau Retrofit.
- Source app icon 512×512 dan feature graphic 1024×500 berhasil dirender dan diunggah.

Artifacts:
- [Debug APK, JUnit/lint reports, Room schemas, wrapper, verification metadata bootstrap, dan release audit](https://github.com/berkah-pos/btp/actions/runs/45989244818/artifacts/15193663522) — retensi 14 hari.
- [Play Store app icon dan feature graphic](https://github.com/berkah-pos/btp/actions/runs/45989244818/artifacts/15192494773) — retensi 90 hari.

**Belum lulus / belum dijalankan:**
- Release AAB job **SKIPPED** pada PR; tidak ada signed AAB, upload-certificate verification, atau release SHA256.
- Strict dependency verification **SKIPPED**. File `gradle/verification-metadata.xml` dari run adalah bootstrap SHA-256 metadata untuk review, bukan baseline tepercaya yang sudah dikomit.
- Instrumentation test Room 1 → 2 belum dieksekusi dengan `connectedDebugAndroidTest`. File schema v1 asli dan test source sudah ada; workflow emulator terpisah ditambahkan pada branch PR sebagai `.github/workflows/room-migration-tests.yml`, tetapi belum ada hasil emulator yang boleh dinyatakan PASS.
- Belum ada bukti startup/perilaku pada perangkat fisik, UI automation, Internal Testing, atau Google Play Pre-launch Report.

Hasil Run #41 berlaku untuk merge commit yang disebutkan di atas. Commit sesudahnya yang menambah workflow Room terpisah tidak mengubah source aplikasi yang diuji oleh Run #41.

## Keputusan teknis

- Kotlin + Jetpack Compose + Material 3.
- MVVM, Coroutines, Flow, Navigation Compose.
- Room/SQLite sebagai sumber data POS lokal; DataStore untuk preferensi ringan.
- Room schema versi 2 diekspor oleh KSP dan dikomit di `app/schemas/`.
- Migrasi Room 1 → 2 menambahkan lima kolom snapshot toko nullable pada `sales_transactions` tanpa menghapus tabel atau baris.
- Schema Room v1 hasil ekspor KSP asli dan schema v2 tersedia di `app/schemas/`; schema v1 ditambahkan melalui commit `be1797ad5ff9eab6bca125d4fdb784589edb6647`.
- `BerkahPosDatabaseMigrationTest` memuat dua skenario migrasi dari bentuk database v1 asli dan bentuk v1 historis antara. Run #41 berhasil mengompilasi test source, tetapi instrumentasi `connectedDebugAndroidTest` belum dijalankan; jangan menyebut migrasi PASS sampai workflow emulator menghasilkan laporan sukses.
- Semua nominal uang disimpan sebagai integer Rupiah (`Long`), bukan Float/Double.
- Tidak ada akun, backend POS, Firebase, analytics, crash reporting, iklan pihak ketiga, atau permission jaringan yang dideklarasikan pada source manifest.
- Android `minSdk=24`, `compileSdk=37.1`, `targetSdk=36`.
- AGP 9.5.3, Gradle 9.9.2, built-in Kotlin/Compose Compiler 2.4.21, KSP 2.3.14, Compose BOM 2026.11.01, Coroutines 1.11.0, JDK 17.

## Identitas aplikasi

Candidate applicationId: `id.berkahtigaputeri.basicpos`. Sebelum signed release, pemilik aplikasi harus mengonfirmasi ID permanen melalui Play Console dan mengisi GitHub Actions Repository Variable `BTP_APPLICATION_ID`. Jangan mengubah applicationId setelah publikasi pertama.

## Riwayat dan review

- [PR #1 — Android foundation](https://github.com/berkah-pos/btp/pull/1), [PR #2 — koreksi dokumentasi pasca-merge](https://github.com/berkah-pos/btp/pull/2), dan [PR #3 — koreksi Android SDK/CI](https://github.com/berkah-pos/btp/pull/3) telah merged pada 27 November 2026.
- [PR #4 — hardening fondasi dan backup](https://github.com/berkah-pos/btp/pull/4) memuat perubahan lanjutan yang divalidasi oleh run #45975233350. Status PR harus dilihat langsung di GitHub; README ini tidak mengasumsikan PR tersebut sudah merged.

## Cloud build

Workflow `.github/workflows/android-ci.yml` memakai standard GitHub-hosted `ubuntu-24.04`, JDK 17, Android SDK API 37.1, Build Tools 36.0.0, dan Gradle 9.9.2. Pull request menuju `main` serta push ke `main` menjalankan unit test, lint, debug build, pemeriksaan `zipalign -c -P 16 -v 4`, dan audit ELF LOAD 16 KB untuk library arm64-v8a/x86_64 bila ada. Signed release AAB hanya melalui `workflow_dispatch` manual pada `main`, GitHub Environment `android-release`, dan upload-key secrets; job release memeriksa `PAGE_ALIGNMENT_16K` dari bundletool serta ELF native library. Gate debug baru menunggu hasil CI pertama, sedangkan gate AAB belum dijalankan karena tidak ada release build. Tidak ada self-hosted runner atau publikasi otomatis.

Runner membangkitkan Gradle Wrapper sementara dari Gradle 9.9.2 yang dipasang oleh action resmi. Distribution SHA-256 dan SHA-256 wrapper JAR diperiksa sebelum wrapper dipakai; wrapper JAR/scripts tidak dikomit. Workflow `.github/workflows/room-migration-tests.yml` dipisahkan dari Android CI utama; workflow itu memakai GitHub-hosted `ubuntu-24.04` dan API 36 emulator, dan hanya terpicu pada perubahan source/schema Room atau `workflow_dispatch`. Definisi workflow ini ada di branch PR tetapi hasil emulator belum tersedia. Perubahan dependency tetap harus diikuti review dan commit `gradle/verification-metadata.xml` sebelum strict dependency verification dapat menjadi gate permanen.

## Konfigurasi promosi dan dukungan

GitHub Actions Repository Variables opsional:
- `BTP_SAAS_URL`: URL HTTPS resmi layanan SaaS.
- `BTP_SUPPORT_EMAIL`: email dukungan resmi.

Keduanya kosong secara default. Jangan mengisi alamat placeholder atau informasi perusahaan yang belum diverifikasi.

## Dokumentasi

- [Arsitektur](docs/ARSITEKTUR.md)
- [Kebijakan Privasi — draf](docs/PRIVACY_POLICY.md)
- [Syarat dan Ketentuan — draf](docs/TERMS_OF_SERVICE.md)
- [Google Play checklist](docs/PLAY_STORE_CHECKLIST.md)
- [Data Safety source review](docs/DATA_SAFETY_REVIEW.md)
- [Release process](docs/RELEASE_PROCESS.md)

## Backup dan privasi

Backup JSON versi 3 berada di lokasi pilihan pengguna dan **tidak dienkripsi oleh aplikasi**. File memuat profil toko, kategori, produk, stok, transaksi, snapshot identitas toko per transaksi, rincian struk, dan preferensi ambang stok rendah. Restore tetap menerima backup versi 2; transaksi lama tanpa snapshot tetap menampilkan identitas toko sebagai tidak tersedia, bukan mengambil profil terbaru. Automatic Android backup dan transfer perangkat dikecualikan melalui manifest serta aturan backup legacy/Android 12+. Backup POS tetap dilakukan manual melalui file JSON yang dipilih pengguna.

## Repository public

Repository ini public. Standard GitHub-hosted runners untuk public repository tidak dikenai biaya menit runner, tetapi artifact storage tetap memiliki kuota dan retensi. Jangan commit signing keystore, password, token, atau data usaha nyata.
