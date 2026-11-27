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

**Android CI [run #79](https://github.com/berkah-pos/btp/actions/runs/46018281138)** selesai dengan kesimpulan **success** pada 27 November 2026 untuk source head `cbeba6fea7576b6557e9e1b935a8f66a8104db91`. Bukti artifact, JUnit XML, lint report, dan job log:

- `testDebugUnitTest`: **29 test, 0 failure, 0 error, 0 skipped**. Lima test baru menguji koordinasi restore DataStore/Room, kegagalan kedua fase, cancellation, dan suppressed exception saat kompensasi gagal.
- `compileDebugAndroidTestKotlin`: **success**.
- `lintDebug`: **0 error, 1 warning** (`OldTargetApi` pada `targetSdk=36`).
- `assembleDebug`: **success**; APK debug diunggah.
- `zipalign -c -P 16 -v 4` dan audit ELF `PT_LOAD` 16 KiB: **success**; keempat library native 64-bit yang dipaketkan lulus.
- Merged release manifest dan release runtime dependency inventory berhasil ditangkap.
- App icon dan feature graphic Google Play berhasil dirender dan diunggah.
- Signed release AAB **SKIPPED** pada pull-request run; tidak ada AAB bertanda tangan yang dihasilkan.

Artifacts:
- [Debug APK, JUnit/lint reports, Room schemas, dependency metadata, dan release audit](https://github.com/berkah-pos/btp/actions/runs/46018281138/artifacts/15208270198) — retensi 14 hari.
- [Play Store icon dan feature graphic](https://github.com/berkah-pos/btp/actions/runs/46018281138/artifacts/15207651178) — retensi 90 hari.

**Room Migration Tests [run #34](https://github.com/berkah-pos/btp/actions/runs/46018281201)** selesai **success** pada 27 November 2026 di GitHub-hosted `ubuntu-24.04` dengan Android API 36 emulator. JUnit XML dan instrumentation exit code mengonfirmasi:

- `connectedDebugAndroidTest`: **2 test, 0 failure, 0 error, 0 skipped**; instrumentation exit code `0`.
- `migratesOriginalKspVersionOneAndPreservesSalesAndReceiptItems`: lulus.
- `migratesHistoricalVersionOneWithProductIdAndReceiptSnapshotsAlreadyPresent`: lulus.
- Kedua fixture SQLite versi 1 dibuka melalui `MIGRATION_1_2` produksi dan schema v2 divalidasi oleh Room.

Artifact:
- [Room migration JUnit report, APKs, dan emulator outputs](https://github.com/berkah-pos/btp/actions/runs/46018281201/artifacts/15207701214).

**Dependency verification review:** metadata dari artifact Android CI Run #79 memiliki SHA-256 `b75c079d854746d701e5312fd11745038d7f1d2052d6818c81e8f69ff812eec6`, dengan **459 komponen dan 754 entri checksum SHA-256 unik**. Dibandingkan metadata Run #58, seluruh 754 koordinat/artifact yang masih dipakai memiliki checksum identik; empat entri lama yang tidak lagi dipakai telah hilang. Metadata mengaktifkan `verify-metadata=true`, tetapi `verify-signatures=false`: ini adalah integrity pinning, bukan autentikasi publisher. Baseline belum dikomit dan clean strict dependency verification **belum lulus**; job strict pada Run #79 dilewati.

**Status yang belum diverifikasi:**
- Clean Gradle dependency verification dengan `--dependency-verification=strict`.
- Signed release AAB, upload-certificate verification, bundletool validation, dan release bundle 16 KiB check.
- Startup/perilaku pada perangkat fisik, UI automation, Internal Testing, dan Google Play Pre-launch Report.
- Application ID permanen, URL Privacy Policy publik, deklarasi Play Console, dan verifikasi identitas organisasi.

## Keputusan teknis

- Kotlin + Jetpack Compose + Material 3.
- MVVM, Coroutines, Flow, Navigation Compose.
- Room/SQLite sebagai sumber data POS lokal; DataStore untuk preferensi ringan.
- Room schema versi 2 diekspor oleh KSP dan dikomit di `app/schemas/`.
- Migrasi Room 1 → 2 menambahkan lima kolom snapshot toko nullable pada `sales_transactions` tanpa menghapus tabel atau baris.
- Schema Room v1 hasil ekspor KSP asli dan schema v2 tersedia di `app/schemas/`; schema v1 ditambahkan melalui commit `be1797ad5ff9eab6bca125d4fdb784589edb6647`.
- `BerkahPosDatabaseMigrationTest` memuat dua skenario migrasi dari bentuk database v1 asli dan bentuk v1 historis antara. Kedua test benar-benar lulus pada API 36 emulator melalui Room Migration Tests [run #34](https://github.com/berkah-pos/btp/actions/runs/46018281201): 2 test, 0 failure/error/skipped.
- Semua nominal uang disimpan sebagai integer Rupiah (`Long`), bukan Float/Double.
- Tidak ada akun, backend POS, Firebase, analytics, crash reporting, iklan pihak ketiga, atau permission jaringan yang dideklarasikan pada source manifest.
- Android `minSdk=24`, `compileSdk=37.1`, `targetSdk=36`.
- AGP 9.5.3, Gradle 9.9.2, built-in Kotlin/Compose Compiler 2.4.21, KSP 2.3.14, Compose BOM 2026.11.01, Coroutines 1.11.0, JDK 17.

## Identitas aplikasi

Candidate applicationId: `id.berkahtigaputeri.basicpos`. Sebelum signed release, pemilik aplikasi harus mengonfirmasi ID permanen melalui Play Console dan mengisi GitHub Actions Repository Variable `BTP_APPLICATION_ID`. Jangan mengubah applicationId setelah publikasi pertama.

## Riwayat dan review

- [PR #1 — Android foundation](https://github.com/berkah-pos/btp/pull/1), [PR #2 — koreksi dokumentasi pasca-merge](https://github.com/berkah-pos/btp/pull/2), dan [PR #3 — koreksi Android SDK/CI](https://github.com/berkah-pos/btp/pull/3) telah merged pada 27 November 2026.
- [PR #4 — hardening fondasi, backup, dan Room migration test](https://github.com/berkah-pos/btp/pull/4) tetap berstatus draft. Source head `cbeba6f` telah divalidasi oleh [Android CI Run #79](https://github.com/berkah-pos/btp/actions/runs/46018281138) dan [Room Migration Tests Run #34](https://github.com/berkah-pos/btp/actions/runs/46018281201). Latest head memiliki tiga check runs sukses: Android CI, Room migration, dan CI admission. Tidak ada PR yang di-merge.

## Cloud build

Workflow `.github/workflows/android-ci.yml` memakai standard GitHub-hosted `ubuntu-24.04`, JDK 17, Android SDK API 37.1, Build Tools 36.0.0, dan Gradle 9.9.2. Android CI Run #79 meluluskan 29 unit tests, lint tanpa error, debug build, 16 KB ZIP/ELF audit, merged manifest, dan runtime dependency inventory; Room Migration Tests Run #34 meluluskan dua migrasi pada API 36 emulator. Signed release AAB hanya melalui `workflow_dispatch` manual pada `main`, GitHub Environment `android-release`, dan upload-key secrets; job release memeriksa `PAGE_ALIGNMENT_16K` dari bundletool serta ELF native library. Input manual `commit_dependency_baseline=true` dibatasi ke `feat/basic-pos-mvp`, memvalidasi checksum/inventory metadata Run #79, lalu menjalankan clean strict quality build pada runner terpisah setelah baseline dikomit. Strict build tersebut belum dijalankan. Tidak ada self-hosted runner atau publikasi otomatis.

Runner membangkitkan Gradle Wrapper sementara dari Gradle 9.9.2 yang dipasang oleh action resmi. Distribution SHA-256 dan SHA-256 wrapper JAR diperiksa sebelum wrapper dipakai; wrapper JAR/scripts tidak dikomit. Workflow `.github/workflows/room-migration-tests.yml` dipisahkan dari Android CI utama dan menjalankan migrasi di API 36 emulator. Run #34 telah meluluskan kedua skenario migrasi. Dependency metadata Run #79 (SHA-256 `b75c079d854746d701e5312fd11745038d7f1d2052d6818c81e8f69ff812eec6`, 459 komponen, 754 hash) cocok pada seluruh 754 entri yang sama dengan Run #58; empat entri plugin metadata lama tidak lagi digunakan. Guard manual untuk mengomit baseline sudah diselaraskan ke checksum dan inventori Run #79, tetapi baseline belum dikomit dan strict verification belum lulus.

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
