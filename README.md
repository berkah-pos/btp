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

**Android CI [run #58](https://github.com/berkah-pos/btp/actions/runs/45999675603)** selesai dengan kesimpulan **success** pada 27 November 2026 untuk commit PR head `aeb015914455606e7b9c985a36e011dbcb3a6137`. Bukti dari GitHub job, log, dan artifact:

- `testDebugUnitTest`: **24 test, 0 failure, 0 error, 0 skipped**, dihitung dari empat JUnit XML.
- `lintDebug`: **0 error, 1 warning** (`OldTargetApi` pada `targetSdk=36`).
- `assembleDebug`: **success**; APK debug diunggah.
- Pemeriksaan APK ZIP alignment 16 KiB dan audit native library berjalan sukses.
- Merged release manifest dan release runtime dependency inventory berhasil ditangkap.
- App icon 512×512 dan feature graphic 1024×500 berhasil dirender dan diunggah.
- Signed release AAB **SKIPPED** karena ini pull-request run. Tidak ada AAB bertanda tangan yang dihasilkan.

Artifacts:
- [Debug APK, JUnit/lint reports, Room schemas, dependency metadata, dan release audit](https://github.com/berkah-pos/btp/actions/runs/45999675603/artifacts/15198380221) — retensi 14 hari.
- [Play Store icon dan feature graphic](https://github.com/berkah-pos/btp/actions/runs/45999675603/artifacts/15198001347) — retensi 90 hari.

**Room Migration Tests [run #17](https://github.com/berkah-pos/btp/actions/runs/45999675609)** selesai **success** pada 27 November 2026 di GitHub-hosted `ubuntu-24.04` dengan Android API 36 emulator. Artifact JUnit terverifikasi menunjukkan:

- `connectedDebugAndroidTest`: **2 test, 0 failure, 0 error, 0 skipped**; instrumentation exit code `0`.
- `migratesOriginalKspVersionOneAndPreservesSalesAndReceiptItems`: lulus.
- `migratesHistoricalVersionOneWithProductIdAndReceiptSnapshotsAlreadyPresent`: lulus.
- Room membuka database versi 1 melalui `MIGRATION_1_2` dan memvalidasi schema versi 2; data transaksi/item lama, snapshot historis, kolom, dan indeks diperiksa.

Artifact:
- [Room migration API 36 instrumentation report dan APK test](https://github.com/berkah-pos/btp/actions/runs/45999675609/artifacts/15197568120) — retensi 30 hari.

Run #14 sebelumnya berhasil boot emulator tetapi gagal sebelum assertion migrasi karena `AbstractMethodError` pada generated Kotlin Serialization serializer di jalur `MigrationTestHelper`. Perbaikan mengganti helper schema-bundle dengan fixture SQLite versi 1 yang eksplisit, lalu membuka fixture melalui Room database builder produksi. Run #17 membuktikan kedua bentuk database historis berhasil dimigrasikan.

**Dependency verification review:** metadata dari artifact Run #58 memiliki SHA-256 `3a9e25b7fb3c0ed4eb21bae8f74e83d1cd2f4c157f03a870e942b6550d0b15c0`, dengan **462 komponen dan 758 entri checksum SHA-256 unik**. Seluruh 758 artefak yang sama dengan metadata Run #41 memiliki checksum identik; tiga entri Room Testing lama tidak lagi dibutuhkan setelah dependensi test helper dihapus. Metadata telah ditinjau untuk struktur, kelengkapan checksum, dan konsistensi antar-run. Ini bukan bukti signature publisher; konfigurasi Gradle masih `verify-signatures=false`. Baseline belum dikomit dan strict mode belum dijalankan; workflow manual yang dibatasi ke branch pengembangan disiapkan untuk commit baseline lalu quality build strict independen.

**Status yang belum diverifikasi:**
- Strict Gradle dependency verification sampai clean strict build menyelesaikan semua task.
- Signed release AAB, upload-certificate verification, bundletool validation, dan release bundle 16 KiB check.
- Startup/perilaku pada perangkat fisik, UI automation, Internal Testing, dan Google Play Pre-launch Report.
- Application ID permanen, URL Privacy Policy publik, deklarasi Play Console, dan identitas organisasi.

## Keputusan teknis

- Kotlin + Jetpack Compose + Material 3.
- MVVM, Coroutines, Flow, Navigation Compose.
- Room/SQLite sebagai sumber data POS lokal; DataStore untuk preferensi ringan.
- Room schema versi 2 diekspor oleh KSP dan dikomit di `app/schemas/`.
- Migrasi Room 1 → 2 menambahkan lima kolom snapshot toko nullable pada `sales_transactions` tanpa menghapus tabel atau baris.
- Schema Room v1 hasil ekspor KSP asli dan schema v2 tersedia di `app/schemas/`; schema v1 ditambahkan melalui commit `be1797ad5ff9eab6bca125d4fdb784589edb6647`.
- `BerkahPosDatabaseMigrationTest` memuat dua skenario migrasi dari bentuk database v1 asli dan bentuk v1 historis antara. Kedua test benar-benar lulus pada API 36 emulator melalui Room Migration Tests [run #17](https://github.com/berkah-pos/btp/actions/runs/45999675609): 2 test, 0 failure/error/skipped.
- Semua nominal uang disimpan sebagai integer Rupiah (`Long`), bukan Float/Double.
- Tidak ada akun, backend POS, Firebase, analytics, crash reporting, iklan pihak ketiga, atau permission jaringan yang dideklarasikan pada source manifest.
- Android `minSdk=24`, `compileSdk=37.1`, `targetSdk=36`.
- AGP 9.5.3, Gradle 9.9.2, built-in Kotlin/Compose Compiler 2.4.21, KSP 2.3.14, Compose BOM 2026.11.01, Coroutines 1.11.0, JDK 17.

## Identitas aplikasi

Candidate applicationId: `id.berkahtigaputeri.basicpos`. Sebelum signed release, pemilik aplikasi harus mengonfirmasi ID permanen melalui Play Console dan mengisi GitHub Actions Repository Variable `BTP_APPLICATION_ID`. Jangan mengubah applicationId setelah publikasi pertama.

## Riwayat dan review

- [PR #1 — Android foundation](https://github.com/berkah-pos/btp/pull/1), [PR #2 — koreksi dokumentasi pasca-merge](https://github.com/berkah-pos/btp/pull/2), dan [PR #3 — koreksi Android SDK/CI](https://github.com/berkah-pos/btp/pull/3) telah merged pada 27 November 2026.
- [PR #4 — hardening fondasi, backup, dan Room migration test](https://github.com/berkah-pos/btp/pull/4) tetap berstatus draft. Source aplikasi pada PR divalidasi oleh [Run #41 attempt 2](https://github.com/berkah-pos/btp/actions/runs/45989244818); commit setelah run tersebut hanya memperbarui workflow dan dokumentasi.

## Cloud build

Workflow `.github/workflows/android-ci.yml` memakai standard GitHub-hosted `ubuntu-24.04`, JDK 17, Android SDK API 37.1, Build Tools 36.0.0, dan Gradle 9.9.2. Run #58 meluluskan unit tests, lint tanpa error, debug build, 16 KB ZIP/ELF audit, merged manifest, dan runtime dependency inventory. Signed release AAB hanya melalui `workflow_dispatch` manual pada `main`, GitHub Environment `android-release`, dan upload-key secrets; job release memeriksa `PAGE_ALIGNMENT_16K` dari bundletool serta ELF native library. Workflow menyediakan input manual terpisah untuk mengomit baseline dependency verification yang sudah ditinjau pada branch pengembangan, lalu menjalankan strict quality build pada runner bersih. Tidak ada self-hosted runner atau publikasi otomatis.

Runner membangkitkan Gradle Wrapper sementara dari Gradle 9.9.2 yang dipasang oleh action resmi. Distribution SHA-256 dan SHA-256 wrapper JAR diperiksa sebelum wrapper dipakai; wrapper JAR/scripts tidak dikomit. Workflow `.github/workflows/room-migration-tests.yml` dipisahkan dari Android CI utama dan menjalankan migrasi di API 36 emulator. Run #17 telah meluluskan kedua skenario migrasi. Dependency metadata Run #58 (SHA-256 `3a9e25b7fb3c0ed4eb21bae8f74e83d1cd2f4c157f03a870e942b6550d0b15c0`, 462 komponen, 758 hash) cocok pada seluruh 758 entri yang sama dengan Run #41; tiga entri lama khusus Room Testing hilang setelah dependensi test helper dihapus. Metadata sudah direview, tetapi belum dikomit dan strict verification belum lulus.

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
