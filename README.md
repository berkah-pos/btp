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

**Android CI [run #50](https://github.com/berkah-pos/btp/actions/runs/45992766270)** selesai dengan kesimpulan **success** pada 27 November 2026 untuk PR head `0ab07e7c5f4a77307d1349029fbbf73452c85c80`. Bukti dari log dan artifact:

- `testDebugUnitTest`: **24 test, 0 failure, 0 error, 0 skipped**, dihitung dari empat JUnit XML.
- `compileDebugAndroidTestKotlin`: **success**; source instrumentation test Room berhasil dikompilasi, tetapi belum dieksekusi.
- `lintDebug`: **0 error, 1 warning** (`OldTargetApi`, karena `targetSdk=36`).
- `assembleDebug`: **success**; APK debug berukuran 14.944.217 byte.
- Pemeriksaan APK ZIP alignment 16 KiB berhasil; keempat ELF LOAD segment pada native library arm64-v8a/x86_64 yang dikemas juga lulus.
- Merged release manifest dan release runtime dependency inventory berhasil ditangkap; app icon dan feature graphic Play Store berhasil dibuat dan diunggah.
- Signed release AAB **SKIPPED** karena run berasal dari pull request. Tidak ada signed AAB atau publikasi yang dihasilkan.

Artifacts:
- [Debug APK, JUnit/lint reports, Room schemas, bootstrap dependency metadata, dan release audit](https://github.com/berkah-pos/btp/actions/runs/45992766270/artifacts/15195500151) — retensi 14 hari.
- [Play Store app icon dan feature graphic](https://github.com/berkah-pos/btp/actions/runs/45992766270/artifacts/15194771833) — retensi 90 hari.

**Room migration workflow:** [Run #9](https://github.com/berkah-pos/btp/actions/runs/45992766223) gagal sebelum `connectedDebugAndroidTest` dijalankan. Artifact diagnostik menunjukkan proses emulator x86_64 tidak memiliki izin baca/tulis ke `/dev/kvm`; emulator melaporkan bahwa akselerasi hardware diperlukan. Ini kegagalan lingkungan runner, bukan kegagalan assertion migrasi. Commit workflow `f1585bd` menambahkan akses ACL khusus untuk user runner (dengan fallback terbatas pada device ephemeral), pemeriksaan akselerator, dan batas waktu ADB. [Run #10](https://github.com/berkah-pos/btp/actions/runs/45997083558) adalah percobaan verifikasi untuk perbaikan tersebut. Migrasi 1 → 2 **belum dinyatakan lulus** sampai test instrumentation benar-benar selesai dan laporan test menunjukkan success.

Schema v1 asli hasil KSP telah dikomit melalui [commit `be1797a`](https://github.com/berkah-pos/btp/commit/be1797ad5ff9eab6bca125d4fdb784589edb6647); schema v2 dan test migrasi untuk schema asli serta bentuk v1 historis juga tersedia di repository.

**Status yang belum diverifikasi:**
- Strict Gradle dependency verification masih **SKIPPED**. Metadata SHA-256 dari CI masih bootstrap TOFU dan belum disetujui/dikomit sebagai baseline tepercaya.
- Signed release AAB, signature/certificate verification, dan release bundle 16 KiB check belum dijalankan.
- Belum ada uji startup/perilaku pada perangkat fisik, UI automation, Internal Testing, atau Google Play Pre-launch Report.

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
- [PR #4 — hardening fondasi, backup, dan Room migration test](https://github.com/berkah-pos/btp/pull/4) tetap berstatus draft. Source aplikasi pada PR divalidasi oleh [Run #41 attempt 2](https://github.com/berkah-pos/btp/actions/runs/45989244818); commit setelah run tersebut hanya memperbarui workflow dan dokumentasi.

## Cloud build

Workflow `.github/workflows/android-ci.yml` memakai standard GitHub-hosted `ubuntu-24.04`, JDK 17, Android SDK API 37.1, Build Tools 36.0.0, dan Gradle 9.9.2. Run #41 attempt 2 sudah meluluskan unit tests, lint tanpa error, debug build, 16 KB ZIP/ELF audit, merged manifest, dan runtime dependency inventory. Signed release AAB hanya melalui `workflow_dispatch` manual pada `main`, GitHub Environment `android-release`, dan upload-key secrets; job release memeriksa `PAGE_ALIGNMENT_16K` dari bundletool serta ELF native library. Strict dependency verification tetap tertahan sampai baseline hash ditinjau dan dikomit. Tidak ada self-hosted runner atau publikasi otomatis.

Runner membangkitkan Gradle Wrapper sementara dari Gradle 9.9.2 yang dipasang oleh action resmi. Distribution SHA-256 dan SHA-256 wrapper JAR diperiksa sebelum wrapper dipakai; wrapper JAR/scripts tidak dikomit. Workflow `.github/workflows/room-migration-tests.yml` dipisahkan dari Android CI utama; workflow itu memakai GitHub-hosted `ubuntu-24.04` dan API 36 emulator, dan hanya terpicu pada perubahan source/schema Room atau `workflow_dispatch`. Definisi workflow ini ada di branch PR tetapi hasil emulator belum tersedia. Metadata bootstrap Run #38 dan Run #41 byte-identical (SHA-256 `45c3de7dd53c00bc4a35afd64a2067fa05d10fe8ab8cb7ef22e86342ebb941b0`, 464 komponen, 761 hash SHA-256), tetapi masih harus direview sebelum dikomit dan dipakai strict.

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
